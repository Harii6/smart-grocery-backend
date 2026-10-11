package com.example.smartdagligvare.Dto.Recipe;

public record RecipeIngredient(
        String searchTerm,
        double amount,
        String unit
) {
}