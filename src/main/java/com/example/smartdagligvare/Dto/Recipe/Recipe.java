package com.example.smartdagligvare.Dto.Recipe;

import java.util.List;

public record Recipe(
        String name,
        int servings,
        int preparationTimeMinutes,
        int caloriesPerServing,
        double proteinGramsPerServing,
        List<RecipeIngredient> ingredients,
        List<String> instructions
) {
}