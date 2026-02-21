package com.amac.ugaras.mappers;

import com.amac.ugaras.models.dtos.installment.InstallmentResponseDto;
import com.amac.ugaras.models.entities.Installment;
import org.springframework.stereotype.Component;

@Component
public class InstallmentMapper {

    public InstallmentResponseDto toDto(Installment installment) {
        return new InstallmentResponseDto(
                installment.getId(),
                installment.getSequenceNumber(),
                installment.getDueDate(),
                installment.getAmountDue(),
                installment.getStatus()
        );
    }
}