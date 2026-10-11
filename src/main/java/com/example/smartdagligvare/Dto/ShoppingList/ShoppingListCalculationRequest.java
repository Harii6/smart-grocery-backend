package com.example.smartdagligvare.Dto.ShoppingList;

import java.util.List;

public record ShoppingListCalculationRequest(
        Long userId,
        List<SelectedProduct> selections
) {
}