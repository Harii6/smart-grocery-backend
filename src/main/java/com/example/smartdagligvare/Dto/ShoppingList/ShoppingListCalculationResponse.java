package com.example.smartdagligvare.Dto.ShoppingList;

import java.math.BigDecimal;
import java.util.List;

public record ShoppingListCalculationResponse(
        Long userId,
        List<SelectedProduct> selections,
        BigDecimal weeklyBudget,
        BigDecimal totalPrice,
        BigDecimal remainingBudget,
        boolean withinBudget,
        List<String> storesUsed,
        int maximumStores,
        boolean withinStoreLimit
) {
}