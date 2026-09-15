package com.example.smartdagligvare.Model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "store_product",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_store_external_product",
                        columnNames = {"chain_id", "external_product_id"}
                )
        }
)
public class StoreProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chain_id", nullable = false)
    private StoreChain storeChain;

    // The product ID used by Kassalapp
    @Column(name = "external_product_id")
    private Long externalProductId;

    @Column(name = "product_url", columnDefinition = "TEXT")
    private String productUrl;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    public StoreProduct() {
    }

    public StoreProduct(
            Product product,
            StoreChain storeChain,
            Long externalProductId,
            String productUrl
    ) {
        this.product = product;
        this.storeChain = storeChain;
        this.externalProductId = externalProductId;
        this.productUrl = productUrl;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public StoreChain getStoreChain() {
        return storeChain;
    }

    public void setStoreChain(StoreChain storeChain) {
        this.storeChain = storeChain;
    }

    public Long getExternalProductId() {
        return externalProductId;
    }

    public void setExternalProductId(Long externalProductId) {
        this.externalProductId = externalProductId;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}