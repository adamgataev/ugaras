package com.amac.ugaras.models.dtos.installment;

import com.amac.ugaras.models.enums.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InstallmentResponseDto(
        UUID id,
        Integer sequenceNumber,
        LocalDate dueDate,
        BigDecimal amountDue,
        InstallmentStatus status
) {}