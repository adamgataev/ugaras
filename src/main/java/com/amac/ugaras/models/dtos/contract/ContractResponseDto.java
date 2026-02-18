package com.amac.ugaras.models.dtos.contract;

import com.amac.ugaras.models.dtos.installment.InstallmentResponseDto;
import com.amac.ugaras.models.enums.ContractStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ContractResponseDto(
        UUID id,
        String buyerName,
        String productName,
        BigDecimal totalSalesPrice,
        BigDecimal downPaymentAmount,
        BigDecimal profitMarginPercentage,
        ContractStatus status,
        LocalDate startDate,
        LocalDate endDate,
        List<InstallmentResponseDto> installments
) {}
