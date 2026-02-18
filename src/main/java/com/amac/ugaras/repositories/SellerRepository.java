package com.amac.ugaras.repositories;

import com.amac.ugaras.models.entities.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SellerRepository extends JpaRepository<Seller, UUID> {
    boolean existsByUserId(UUID userId);
}
