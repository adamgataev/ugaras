package com.amac.ugaras.controllers;

import com.amac.ugaras.models.entities.Contract;
import com.amac.ugaras.models.entities.Installment;
import com.amac.ugaras.models.dtos.contract.*;
import com.amac.ugaras.models.dtos.installment.InstallmentResponseDto;
import com.amac.ugaras.services.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<ContractResponseDto> createContract(@Valid @RequestBody ContractRequestDto request) {
        // Naamswijziging hier doorgevoerd
        Contract contract = contractService.createContract(request);

        ContractResponseDto response = mapToDto(contract);

        return ResponseEntity
                .created(URI.create("/api/v1/contracts/" + contract.getId()))
                .body(response);
    }

    private ContractResponseDto mapToDto(Contract contract) {
        List<InstallmentResponseDto> installmentDtos = contract.getInstallments().stream()
                .map(this::mapInstallmentToDto)
                .toList();

        return new ContractResponseDto(
                contract.getId(),
                contract.getBuyer().getFirstName() + " " + contract.getBuyer().getLastName(),
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

    private InstallmentResponseDto mapInstallmentToDto(Installment installment) {
        return new InstallmentResponseDto(
                installment.getId(),
                installment.getSequenceNumber(),
                installment.getDueDate(),
                installment.getAmountDue(),
                installment.getStatus()
        );
    }
}