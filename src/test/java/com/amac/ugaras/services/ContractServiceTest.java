package com.amac.ugaras.services;

import com.amac.ugaras.exceptions.ResourceNotFoundException;
import com.amac.ugaras.mappers.ContractMapper;
import com.amac.ugaras.mappers.InstallmentMapper;
import com.amac.ugaras.models.dtos.contract.ContractResponseDto;
import com.amac.ugaras.models.dtos.installment.InstallmentResponseDto;
import com.amac.ugaras.models.entities.Buyer;
import com.amac.ugaras.models.entities.Contract;
import com.amac.ugaras.models.entities.Product;
import com.amac.ugaras.models.dtos.contract.ContractRequestDto;
import com.amac.ugaras.repositories.BuyerRepository;
import com.amac.ugaras.repositories.ContractRepository;
import com.amac.ugaras.repositories.ProductRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock private ContractRepository contractRepository;
    @Mock private ProductRepository productRepository;
    @Mock private BuyerRepository buyerRepository;
    @Mock private MessageSource messageSource;
    @Mock private ContractMapper contractMapper;

    @InjectMocks
    private ContractService contractService;

    private final InstallmentMapper installmentMapper = new InstallmentMapper();

    private final Faker faker = new Faker();
    private Product product;
    private Buyer buyer;

    @BeforeEach
    void setUp() {
        // Build base data without ID (using standard @Builder)
        product = Product.builder()
                .price(new BigDecimal("1000.00")) // Cost price 1000
                .name(faker.commerce().productName())
                .build();
        // Set ID manually (Fixing the inheritance issue)
        product.setId(UUID.randomUUID());

        buyer = Buyer.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .build();
        buyer.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should correctly calculate Cost-Plus contract figures")
    void shouldCalculateCostPlusCorrectly() {
        // Arrange
        var request = new ContractRequestDto(
                buyer.getId(),
                product.getId(),
                new BigDecimal("200.00"), // Down payment
                new BigDecimal("10.00"),  // 10% Margin
                LocalDate.now(),
                10 // 10 Installments
        );

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(buyerRepository.findById(buyer.getId())).thenReturn(Optional.of(buyer));
        when(contractRepository.save(any(Contract.class))).thenAnswer(i -> i.getArguments()[0]);
        when(contractMapper.toDto(any(Contract.class))).thenAnswer(inv -> {
            Contract c = inv.getArgument(0);
            List<InstallmentResponseDto> instDtos = c.getInstallments().stream()
                    .map(installmentMapper::toDto)
                    .toList();
            return new ContractResponseDto(
                    c.getId(), c.getBuyer().getDisplayName(), c.getProduct().getName(),
                    c.getTotalSalesPrice(), c.getDownPaymentAmount(), c.getProfitMarginPercentage(),
                    c.getStatus(), c.getStartDate(), c.getEndDate(), instDtos);
        });

        // Act
        ContractResponseDto result = contractService.createContract(request);

        // Assert - Calculation check:
        // Financed = 1000 - 200 = 800
        // Profit = 800 * 10% = 80
        // Total Sales Price = 1000 + 80 = 1080
        // Total Repayment = 800 + 80 = 880

        assertThat(result.totalSalesPrice()).isEqualByComparingTo("1080.00");

        // Check Installments (880 / 10 = 88 per month)
        assertThat(result.installments()).hasSize(10);
        assertThat(result.installments().getFirst().amountDue()).isEqualByComparingTo("88.00");

        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    @DisplayName("Should handle rounding differences in the last installment")
    void shouldHandleRoundingInLastInstallment() {
        // Arrange: Repay 100 euro in 3 installments (33.33, 33.33, 33.34)
        product.setPrice(new BigDecimal("100.00"));

        var request = new ContractRequestDto(
                buyer.getId(),
                product.getId(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                LocalDate.now(),
                3
        );

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(buyerRepository.findById(buyer.getId())).thenReturn(Optional.of(buyer));
        when(contractRepository.save(any(Contract.class))).thenAnswer(i -> i.getArguments()[0]);
        when(contractMapper.toDto(any(Contract.class))).thenAnswer(inv -> {
            Contract c = inv.getArgument(0);
            List<InstallmentResponseDto> instDtos = c.getInstallments().stream()
                    .map(installmentMapper::toDto)
                    .toList();
            return new ContractResponseDto(
                    c.getId(), c.getBuyer().getDisplayName(), c.getProduct().getName(),
                    c.getTotalSalesPrice(), c.getDownPaymentAmount(), c.getProfitMarginPercentage(),
                    c.getStatus(), c.getStartDate(), c.getEndDate(), instDtos);
        });

        // Act
        ContractResponseDto result = contractService.createContract(request);

        // Assert
        var installments = result.installments();
        assertThat(installments).hasSize(3);

        // Installment 1 & 2
        assertThat(installments.get(0).amountDue()).isEqualByComparingTo("33.33");
        assertThat(installments.get(1).amountDue()).isEqualByComparingTo("33.33");

        // Installment 3 (Must contain the remainder)
        assertThat(installments.get(2).amountDue()).isEqualByComparingTo("33.34");

        // Total must be exactly 100
        BigDecimal sum = installments.stream()
                .map(InstallmentResponseDto::amountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sum).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Should throw exception when down payment is higher than cost price")
    void shouldThrowExceptionIfDownPaymentTooHigh() {
        var request = new ContractRequestDto(
                buyer.getId(),
                product.getId(),
                new BigDecimal("1500.00"), // Higher than 1000
                BigDecimal.TEN,
                LocalDate.now(),
                12
        );

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(buyerRepository.findById(buyer.getId())).thenReturn(Optional.of(buyer));
        // Mock message source to avoid NPE
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Error Message");

        assertThatThrownBy(() -> contractService.createContract(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("getContractById throws ResourceNotFoundException when contract not found")
    void getContractByIdThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(contractRepository.findById(id)).thenReturn(Optional.empty());
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Contract not found");

        assertThatThrownBy(() -> contractService.getContractById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Contract not found");

        verify(contractMapper, never()).toDto(any());
    }
}