package com.amac.ugaras.services;

import com.amac.ugaras.models.entities.*;
import com.amac.ugaras.models.dtos.contract.ContractRequestDto;
import com.amac.ugaras.models.enums.*;
import com.amac.ugaras.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final ProductRepository productRepository;
    private final BuyerRepository buyerRepository;
    private final MessageSource messageSource;

    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    @Transactional
    public Contract createContract(ContractRequestDto request) {
        // 1. Valideer entiteiten
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException(
                        getMessage("exception.product.not.found", request.productId())
                ));

        Buyer buyer = buyerRepository.findById(request.buyerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        getMessage("exception.buyer.not.found", request.buyerId())
                ));

        // 2. Berekeningen (Cost-Plus)
        BigDecimal costPrice = product.getPrice();
        BigDecimal downPayment = request.downPaymentAmount();
        BigDecimal marginPercent = request.profitMarginPercentage();

        // Validatie: Aanbetaling < Kostprijs
        if (downPayment.compareTo(costPrice) >= 0) {
            throw new IllegalArgumentException(
                    getMessage("exception.downpayment.too.high", downPayment, costPrice)
            );
        }

        // Formule: Gefinancierd bedrag
        BigDecimal financedAmount = costPrice.subtract(downPayment);

        // Formule: Winstbedrag
        BigDecimal profitAmount = financedAmount
                .multiply(marginPercent)
                .divide(BigDecimal.valueOf(100), 4, ROUNDING);

        // Formule: Totale Verkoopprijs
        BigDecimal totalSalesPrice = costPrice.add(profitAmount);

        // Formule: Totaal terug te betalen
        BigDecimal totalRepaymentAmount = financedAmount.add(profitAmount);

        // Afronden voor opslag
        totalSalesPrice = totalSalesPrice.setScale(2, ROUNDING);
        totalRepaymentAmount = totalRepaymentAmount.setScale(2, ROUNDING);

        // 3. Bouw Contract
        Contract contract = Contract.builder()
                .buyer(buyer)
                .product(product)
                .costPrice(costPrice)
                .downPaymentAmount(downPayment)
                .profitMarginPercentage(marginPercent)
                .totalSalesPrice(totalSalesPrice)
                .totalRepaymentAmount(totalRepaymentAmount)
                .status(ContractStatus.ACTIVE)
                .startDate(request.startDate())
                .endDate(request.startDate().plusMonths(request.numberOfInstallments()))
                .build();

        // 4. Genereer Termijnen
        List<Installment> installments = generateInstallments(contract, totalRepaymentAmount, request.numberOfInstallments());
        contract.setInstallments(installments);

        return contractRepository.save(contract);
    }

    private List<Installment> generateInstallments(Contract contract, BigDecimal totalToRepay, int months) {
        List<Installment> list = new ArrayList<>();
        BigDecimal monthsBd = BigDecimal.valueOf(months);

        BigDecimal baseMonthlyAmount = totalToRepay.divide(monthsBd, 2, RoundingMode.DOWN);
        BigDecimal currentTotal = BigDecimal.ZERO;

        for (int i = 1; i <= months; i++) {
            BigDecimal amountDue;

            if (i == months) {
                // Laatste termijn pakt het restant
                amountDue = totalToRepay.subtract(currentTotal);
            } else {
                amountDue = baseMonthlyAmount;
            }

            currentTotal = currentTotal.add(amountDue);

            Installment installment = Installment.builder()
                    .contract(contract)
                    .sequenceNumber(i)
                    .dueDate(contract.getStartDate().plusMonths(i))
                    .amountDue(amountDue)
                    .amountPaid(BigDecimal.ZERO)
                    .status(InstallmentStatus.PENDING)
                    .build();

            list.add(installment);
        }

        if (currentTotal.compareTo(totalToRepay) != 0) {
            throw new IllegalStateException(
                    getMessage("exception.calculation.error", currentTotal, totalToRepay)
            );
        }

        return list;
    }

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}