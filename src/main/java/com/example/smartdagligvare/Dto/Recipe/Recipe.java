package com.example.smartdagligvare.Dto.Recipe;

import java.util.List;

public record Recipe(
        String name,
        int servings,
        List<RecipeIngredient> ingredients,
        List<String> instructions
) {
}