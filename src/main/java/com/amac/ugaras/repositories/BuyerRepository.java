package com.amac.ugaras.repositories;

import com.amac.ugaras.models.entities.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BuyerRepository extends JpaRepository<Buyer, UUID> {
}
