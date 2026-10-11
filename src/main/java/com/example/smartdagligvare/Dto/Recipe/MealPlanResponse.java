package com.example.smartdagligvare.Dto.Recipe;

import java.util.List;

public record MealPlanResponse(
        List<Recipe> recipes
) {
}