package com.example.smartdagligvare.Model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "price",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_store_product_recorded_at",
                        columnNames = {"store_product_id", "recorded_at"}
                )
        }
)
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "store_product_id", nullable = false)
    private StoreProduct storeProduct;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    public Price() {
    }

    public Price(
            StoreProduct storeProduct,
            BigDecimal amount,
            BigDecimal unitPrice,
            Instant recordedAt
    ) {
        this.storeProduct = storeProduct;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.recordedAt = recordedAt;
    }

    public Long getId() {
        return id;
    }

    public StoreProduct getStoreProduct() {
        return storeProduct;
    }

    public void setStoreProduct(StoreProduct storeProduct) {
        this.storeProduct = storeProduct;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }
}