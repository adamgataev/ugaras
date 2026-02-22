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
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT") // Zodat het TEXT wordt in PostgreSQL en niet VARCHAR
    private String description;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageURL;
}
