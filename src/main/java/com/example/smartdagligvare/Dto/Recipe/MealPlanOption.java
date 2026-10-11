package com.example.smartdagligvare.Dto.Recipe;

import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListResponse;

public record MealPlanOption(
        Recipe recipe,
        ShoppingListResponse shoppingList,
        boolean valid
) {
}