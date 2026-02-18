package com.amac.ugaras.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;


@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Payment extends BaseEntity{
    // Directe link naar contract (voor snelle totalen)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    // Link naar specifieke termijn (kan null zijn bijv. bij een aanbetaling vooraf)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_id")
    private Installment installment;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "transaction_reference", unique = true)
    private String transactionReference;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Builder.Default
    @Column(name = "payment_date")
    private Instant paymentDate = Instant.now();
}
