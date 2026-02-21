package com.amac.ugaras.controllers;

import com.amac.ugaras.models.dtos.contract.*;
import com.amac.ugaras.services.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<ContractResponseDto> createContract(@Valid @RequestBody ContractRequestDto request) {
        ContractResponseDto response = contractService.createContract(request);

        return ResponseEntity
                .created(URI.create("/api/v1/contracts/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractResponseDto> getContractById(@PathVariable UUID id) {
        ContractResponseDto response = contractService.getContractById(id);

        return ResponseEntity.ok(response);
    }
}