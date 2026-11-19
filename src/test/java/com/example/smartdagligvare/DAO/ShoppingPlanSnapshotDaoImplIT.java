package com.example.smartdagligvare.Model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class ShoppingPlanSnapshotTest {
    @Test
    void comparisonRequiresBothTotals() {
        assertThrows(IllegalArgumentException.class,
                () -> new ShoppingPlanSnapshot(7L, BigDecimal.TEN, null, "test", "{}"));
    }

    @Test
    void recommendedBasketMustNotCostMoreThanBaseline() {
        assertThrows(IllegalArgumentException.class,
                () -> new ShoppingPlanSnapshot(7L, BigDecimal.TEN, new BigDecimal("11"), "test", "{}"));
    }

    @Test
    void unavailableComparisonRequiresExplanation() {
        assertThrows(IllegalArgumentException.class,
                () -> new ShoppingPlanSnapshot(7L, null, null, null, "{}"));
    }

    @Test
    void confirmationIsIdempotent() {
        ShoppingPlanSnapshot snapshot = new ShoppingPlanSnapshot(7L,
                new BigDecimal("100.00"), new BigDecimal("80.00"), "test", "{}");
        Instant first = snapshot.getCreatedAt().plusSeconds(1);
        snapshot.confirm(first);
        snapshot.confirm(first.plusSeconds(100));
        assertEquals(first, snapshot.getConfirmedAt());
        assertEquals(0, new BigDecimal("20.00").compareTo(snapshot.getEstimatedSavings()));
    }
}
