package com.amac.ugaras.repositories;

import com.amac.ugaras.models.entities.Contract;
import com.amac.ugaras.models.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    List<Contract> findByBuyerId(UUID buyerId);

    List<Contract> findByStatus(ContractStatus status);

    List<Contract> findByProduct_SellerId(UUID sellerId);
}
