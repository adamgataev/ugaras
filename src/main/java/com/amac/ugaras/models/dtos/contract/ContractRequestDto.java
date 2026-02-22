package com.amac.ugaras.models.dtos.contract;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContractRequestDto(
        @NotNull(message = "{contract.buyer.required}")
        UUID buyerId,

        @NotNull(message = "{contract.product.required}")
        UUID productId,

        @NotNull(message = "{contract.costprice.required}")
        @Digits(integer = 15, fraction = 2, message = "{contract.costprice.digits}")
        @DecimalMin(value = "0.01", message = "{contract.costprice.min}")
        BigDecimal costPrice,

        @NotNull(message = "{contract.downpayment.required}")
        @Digits(integer = 15, fraction = 2, message = "{contract.downpayment.digits}")
        @DecimalMin(value = "0.00", message = "{contract.downpayment.min}")
        BigDecimal downPaymentAmount,

        @NotNull(message = "{contract.margin.required}")
        @Digits(integer = 3, fraction = 2, message = "{contract.margin.digits}")
        @DecimalMin(value = "0.00", message = "{contract.margin.min}")
        BigDecimal profitMarginPercentage,

        @NotNull(message = "{contract.startdate.required}")
        LocalDate startDate,

        @Min(value = 1, message = "{contract.installments.min}")
        @Max(value = 60, message = "{contract.installments.max}")
        int numberOfInstallments
) {}