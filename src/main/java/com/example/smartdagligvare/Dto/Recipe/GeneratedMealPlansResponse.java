package com.example.smartdagligvare.Dto.Recipe;

import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListResponse;

import java.math.BigDecimal;
import java.util.List;

public record GeneratedMealPlansResponse(
        Long userId,
        BigDecimal weeklyBudget,
        List<Recipe> recipes,
        ShoppingListResponse shoppingList
) {
}