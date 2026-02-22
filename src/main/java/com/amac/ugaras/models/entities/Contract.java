package com.amac.ugaras.models.entities;

import com.amac.ugaras.models.enums.ContractStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Contract extends BaseEntity{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    @Column(name = "cost_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal costPrice;

    @Builder.Default
    @Column(name = "down_payment_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal downPaymentAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "profit_margin_percentage", precision = 5, scale = 2)
    private BigDecimal profitMarginPercentage = BigDecimal.ZERO;

    @Column(name = "total_repayment_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalRepaymentAmount;

    @Column(name = "total_sales_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalSalesPrice;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(columnDefinition = "contract_status") // Postgres enum type hint
    private ContractStatus status = ContractStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    private List<Installment> installments = new ArrayList<>();
}
