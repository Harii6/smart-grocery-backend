package com.example.smartdagligvare.Dto.ShoppingList;

import java.math.BigDecimal;
import java.util.List;

public record ShoppingListResponse(
        Long userId,
        BigDecimal weeklyBudget,
        List<ShoppingListItem> items,
        List<String> missingIngredients,
        StoreRecommendation recommendation
) {
}