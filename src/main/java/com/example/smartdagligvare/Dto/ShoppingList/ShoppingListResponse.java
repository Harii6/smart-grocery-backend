package com.example.smartdagligvare.Dto.ShoppingList;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ShoppingListResponse(
        Long userId,
        BigDecimal weeklyBudget,
        List<ShoppingListItem> items,
        List<String> missingIngredients,
        StoreRecommendation recommendation,
        UUID snapshotId
) {

    /*
     * Keeps existing five-argument constructor calls working.
     * Before saving, a response has no snapshot ID.
     */
    public ShoppingListResponse(
            Long userId,
            BigDecimal weeklyBudget,
            List<ShoppingListItem> items,
            List<String> missingIngredients,
            StoreRecommendation recommendation
    ) {
        this(
                userId,
                weeklyBudget,
                items,
                missingIngredients,
                recommendation,
                null
        );
    }
}