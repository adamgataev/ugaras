package com.amac.ugaras.models.dtos.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponseDto(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        String sku,
        UUID sellerId,
        String sellerCompanyName,
        Instant createdAt,
        Instant updatedAt
){}
