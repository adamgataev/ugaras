package com.amac.ugaras.mappers;

import com.amac.ugaras.models.dtos.contract.ContractResponseDto;
import com.amac.ugaras.models.dtos.installment.InstallmentResponseDto;
import com.amac.ugaras.models.entities.Contract;
import com.amac.ugaras.models.entities.Installment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor // Used so that Spring injects the InstallmentMapper
public class ContractMapper {

    private final InstallmentMapper installmentMapper;

    public ContractResponseDto toDto(Contract contract) {
        List<Installment> installments = Optional.ofNullable(contract.getInstallments())
                .orElse(Collections.emptyList());
        List<InstallmentResponseDto> installmentDtos = installments.stream()
                .map(installmentMapper::toDto)
                .toList();

        return new ContractResponseDto(
                contract.getId(),
                contract.getBuyer().getDisplayName(),
                contract.getProduct().getName(),
                contract.getTotalSalesPrice(),
                contract.getDownPaymentAmount(),
                contract.getProfitMarginPercentage(),
                contract.getStatus(),
                contract.getStartDate(),
                contract.getEndDate(),
                installmentDtos
        );
    }
}