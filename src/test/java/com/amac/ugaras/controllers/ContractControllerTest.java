package com.amac.ugaras.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.amac.ugaras.models.entities.Buyer;
import com.amac.ugaras.models.entities.Contract;
import com.amac.ugaras.models.entities.Product;
import com.amac.ugaras.models.dtos.contract.ContractRequestDto;
import com.amac.ugaras.services.ContractService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContractController.class)
class ContractControllerTest {

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
        UUID buyerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        ContractRequestDto request = new ContractRequestDto(
                buyerId, productId, new BigDecimal("100.00"), new BigDecimal("10.00"), LocalDate.now(), 12
        );

        // Mock service response
        // Using setters for ID to avoid builder issues in tests
        Buyer buyer = Buyer.builder().firstName("Test").lastName("User").build();
        buyer.setId(buyerId);

        Product product = Product.builder().name("Test Product").build();
        product.setId(productId);

        Contract dummyContract = Contract.builder()
                .buyer(buyer)
                .product(product)
                .totalSalesPrice(BigDecimal.TEN)
                .installments(new ArrayList<>())
                .startDate(LocalDate.now())
                .build();
        dummyContract.setId(UUID.randomUUID());

        when(contractService.createContract(any(ContractRequestDto.class))).thenReturn(dummyContract);

        // Act & Assert
        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
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
}