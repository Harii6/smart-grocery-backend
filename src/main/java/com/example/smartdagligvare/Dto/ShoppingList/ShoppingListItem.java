package com.example.smartdagligvare.Dto.ShoppingList;

import java.util.List;

public record ShoppingListItem(
        String ingredient,
        String category,
        double amount,
        String unit,
        List<ProductOption> options
) {
}