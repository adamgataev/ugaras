package com.amac.ugaras.models.dtos.product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequestDto(
        @NotBlank(message = "Productnaam is verplicht")
        @Size(max = 255, message = "Naam mag max 255 tekens zijn")
        String name,
        String description,
        @NotNull(message = "Prijs is verplicht")
        @DecimalMin(value = "0.00", inclusive = false, message = "Prijs moet groter zijn dan 0")
        @Digits(integer = 15, fraction = 2, message = "Prijs ongeldig formaat")
        BigDecimal price,
        @Size(max = 50)
        String sku,
        @NotNull(message = "Seller ID is verplicht")
        UUID sellerId
) {}
