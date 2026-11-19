package com.example.smartdagligvare.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "shopping_plan_snapshot")
public class ShoppingPlanSnapshot {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(nullable = false, length = 3, updatable = false)
    private String currency;

    @Column(
            name = "comparison_method",
            nullable = false,
            length = 50,
            updatable = false
    )
    private String comparisonMethod;

    @Column(
            name = "baseline_total",
            precision = 12,
            scale = 2,
            updatable = false
    )
    private BigDecimal baselineTotal;

    @Column(
            name = "recommended_total",
            precision = 12,
            scale = 2,
            updatable = false
    )
    private BigDecimal recommendedTotal;

    @Column(
            name = "comparison_note",
            columnDefinition = "text",
            updatable = false
    )
    private String comparisonNote;

    @Column(
            name = "snapshot_json",
            nullable = false,
            columnDefinition = "text",
            updatable = false
    )
    private String snapshotJson;

    protected ShoppingPlanSnapshot() {
        // Required by JPA.
    }

    public ShoppingPlanSnapshot(
            Long userId,
            BigDecimal baselineTotal,
            BigDecimal recommendedTotal,
            String comparisonNote,
            String snapshotJson
    ) {
        this.userId = Objects.requireNonNull(
                userId,
                "User ID is required."
        );

        if (snapshotJson == null || snapshotJson.isBlank()) {
            throw new IllegalArgumentException(
                    "Snapshot JSON is required."
            );
        }

        if ((baselineTotal == null) != (recommendedTotal == null)) {
            throw new IllegalArgumentException(
                    "Both comparison totals must be provided, or both must be null."
            );
        }

        if (baselineTotal != null) {
            if (baselineTotal.signum() < 0 ||
                    recommendedTotal.signum() < 0) {
                throw new IllegalArgumentException(
                        "Comparison totals cannot be negative."
                );
            }

            if (recommendedTotal.compareTo(baselineTotal) > 0) {
                throw new IllegalArgumentException(
                        "The recommended total cannot exceed the comparison total."
                );
            }

            this.baselineTotal = baselineTotal.setScale(
                    2,
                    RoundingMode.UNNECESSARY
            );

            this.recommendedTotal = recommendedTotal.setScale(
                    2,
                    RoundingMode.UNNECESSARY
            );
        } else if (comparisonNote == null || comparisonNote.isBlank()) {
            throw new IllegalArgumentException(
                    "Explain why the comparison is unavailable."
            );
        }

        this.id = UUID.randomUUID();
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        this.currency = "NOK";
        this.comparisonMethod = "BEST_SINGLE_STORE_V1";
        this.comparisonNote = comparisonNote;
        this.snapshotJson = snapshotJson;
    }

    public void confirm(Instant confirmationTime) {
        // Repeated confirmation must not change the original date.
        if (confirmedAt != null) {
            return;
        }

        Objects.requireNonNull(
                confirmationTime,
                "Confirmation time is required."
        );

        Instant timestamp = confirmationTime.truncatedTo(
                ChronoUnit.MICROS
        );

        if (timestamp.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "Confirmation cannot be before plan creation."
            );
        }

        confirmedAt = timestamp;
    }

    public boolean isComparisonAvailable() {
        return baselineTotal != null && recommendedTotal != null;
    }

    public BigDecimal getEstimatedSavings() {
        if (!isComparisonAvailable()) {
            return null;
        }

        return baselineTotal.subtract(recommendedTotal);
    }

    public UUID getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public String getCurrency() {
        return currency;
    }

    public String getComparisonMethod() {
        return comparisonMethod;
    }

    public BigDecimal getBaselineTotal() {
        return baselineTotal;
    }

    public BigDecimal getRecommendedTotal() {
        return recommendedTotal;
    }

    public String getComparisonNote() {
        return comparisonNote;
    }

    public String getSnapshotJson() {
        return snapshotJson;
    }
}