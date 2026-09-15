package com.example.smartdagligvare.Model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "physical_store")
public class PhysicalStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID received from Kassalapp
    @Column(name = "external_id", nullable = false, unique = true)
    private Long externalId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chain_id", nullable = false)
    private StoreChain storeChain;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    public PhysicalStore() {
    }

    public PhysicalStore(
            Long externalId,
            StoreChain storeChain,
            String name,
            String address,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        this.externalId = externalId;
        this.storeChain = storeChain;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() {
        return id;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public StoreChain getStoreChain() {
        return storeChain;
    }

    public void setStoreChain(StoreChain storeChain) {
        this.storeChain = storeChain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
}