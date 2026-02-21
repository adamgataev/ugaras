package com.amac.ugaras.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.amac.ugaras.exceptions.GlobalExceptionHandler;
import com.amac.ugaras.exceptions.ResourceNotFoundException;
import com.amac.ugaras.models.dtos.contract.ContractRequestDto;
import com.amac.ugaras.models.dtos.contract.ContractResponseDto;
import com.amac.ugaras.models.enums.ContractStatus;
import com.amac.ugaras.services.ContractService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContractController.class)
@Import({ GlobalExceptionHandler.class, ContractControllerTest.ObjectMapperConfig.class })
class ContractControllerTest {

@TestConfiguration
        static class ObjectMapperConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().registerModule(new JavaTimeModule());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContractService contractService;

    @Test
    @DisplayName("POST /api/v1/contracts - Valid Request returns 201 Created")
    void createContractSuccess() throws Exception {
        // Arrange
        UUID contractId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now();

        ContractRequestDto request = new ContractRequestDto(
                buyerId, productId, new BigDecimal("100.00"), new BigDecimal("10.00"), startDate, 12
        );

        ContractResponseDto responseDto = new ContractResponseDto(
                contractId,
                "Test User",
                "Test Product",
                BigDecimal.TEN,
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                ContractStatus.ACTIVE,
                startDate,
                startDate.plusMonths(12),
                List.of()
        );

        when(contractService.createContract(any(ContractRequestDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/contracts/" + contractId))
                .andExpect(jsonPath("$.id").value(contractId.toString()))
                .andExpect(jsonPath("$.buyerName").value("Test User"));
    }

    @Test
    @DisplayName("Validation - Should return Dutch error message when header is set")
    void createContractValidationFailureDutch() throws Exception {
        // Arrange - Invalid down payment (negative)
        ContractRequestDto invalidRequest = new ContractRequestDto(
                UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("-100.00"),
                new BigDecimal("10.00"), LocalDate.now(), 12
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "nl-NL") // Requesting Dutch
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                // Testing that the properties file is loaded correctly
                .andExpect(jsonPath("$.downPaymentAmount").value("Aanbetaling mag niet negatief zijn."));
    }

    @Test
    @DisplayName("Validation - Should return English error message by default")
    void createContractValidationFailureEnglish() throws Exception {
        // Arrange
        ContractRequestDto invalidRequest = new ContractRequestDto(
                UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("-100.00"),
                new BigDecimal("10.00"), LocalDate.now(), 12
        );

        // Act & Assert (No header provided)
        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                // Testing English fallback
                .andExpect(jsonPath("$.downPaymentAmount").value("Down payment cannot be negative."));
    }

    @Test
    @DisplayName("GET /api/v1/contracts/{id} - Returns 200 with contract when found")
    void getContractByIdSuccess() throws Exception {
        UUID contractId = UUID.randomUUID();
        ContractResponseDto responseDto = new ContractResponseDto(
                contractId, "Test User", "Test Product", BigDecimal.TEN,
                BigDecimal.ONE, BigDecimal.ZERO, ContractStatus.ACTIVE,
                LocalDate.now(), LocalDate.now().plusMonths(12), List.of()
        );
        when(contractService.getContractById(contractId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/contracts/" + contractId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractId.toString()))
                .andExpect(jsonPath("$.buyerName").value("Test User"));
    }

    @Test
    @DisplayName("GET /api/v1/contracts/{id} - Returns 404 when contract not found")
    void getContractByIdNotFound() throws Exception {
        UUID contractId = UUID.randomUUID();
        when(contractService.getContractById(contractId))
                .thenThrow(new ResourceNotFoundException("Contract with ID " + contractId + " not found."));

        mockMvc.perform(get("/api/v1/contracts/" + contractId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Contract with ID " + contractId + " not found."));
    }
}