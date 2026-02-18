package com.amac.ugaras.repositories;

import com.amac.ugaras.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Spring maakt de query automatisch op basis van de methodenaam
    Optional<User> findByEmail(String email);
}