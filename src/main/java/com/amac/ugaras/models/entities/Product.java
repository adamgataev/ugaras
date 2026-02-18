package com.amac.ugaras.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Product extends BaseEntity{
    // Meerdere producten horen bij 1 verkoper. Later misschien many to many hier. Voorlopig nog niet nodig.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT") // Zodat het TEXT wordt in PostgreSQL en niet VARCHAR
    private String description;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(length = 50)
    private String sku;
}
