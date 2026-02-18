package com.amac.ugaras.services;

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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @InjectMocks
    private ContractService contractService;

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

        // Act
        Contract result = contractService.createContract(request);

        // Assert - Calculation check:
        // Financed = 1000 - 200 = 800
        // Profit = 800 * 10% = 80
        // Total Sales Price = 1000 + 80 = 1080
        // Total Repayment = 800 + 80 = 880

        assertThat(result.getCostPrice()).isEqualByComparingTo("1000.00");
        assertThat(result.getTotalSalesPrice()).isEqualByComparingTo("1080.00");
        assertThat(result.getTotalRepaymentAmount()).isEqualByComparingTo("880.00");

        // Check Installments (880 / 10 = 88 per month)
        assertThat(result.getInstallments()).hasSize(10);
        assertThat(result.getInstallments().getFirst().getAmountDue()).isEqualByComparingTo("88.00");

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

        // Act
        Contract result = contractService.createContract(request);

        // Assert
        var installments = result.getInstallments();
        assertThat(installments).hasSize(3);

        // Installment 1 & 2
        assertThat(installments.get(0).getAmountDue()).isEqualByComparingTo("33.33");
        assertThat(installments.get(1).getAmountDue()).isEqualByComparingTo("33.33");

        // Installment 3 (Must contain the remainder)
        assertThat(installments.get(2).getAmountDue()).isEqualByComparingTo("33.34");

        // Total must be exactly 100
        BigDecimal sum = installments.stream()
                .map(i -> i.getAmountDue())
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
}