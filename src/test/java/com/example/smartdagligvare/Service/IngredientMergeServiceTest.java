package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.Dto.Recipe.Recipe;
import com.example.smartdagligvare.Dto.Recipe.RecipeIngredient;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientMergeServiceTest {

    private final IngredientMergeService service =
            new IngredientMergeService();

    @Test
    void mergesSameIngredientAndUnit() {
        Recipe first = recipe(
                ingredient("gulrøtter", 300, "g")
        );
        Recipe second = recipe(
                ingredient("gulrøtter", 200, "g")
        );

        List<RecipeIngredient> result =
                service.merge(List.of(first, second));

        assertEquals(1, result.size());
        assertEquals("gulrøtter", result.getFirst().searchTerm());
        assertEquals(500.0, result.getFirst().amount(), 0.001);
    }

    @Test
    void matchesTermsRegardlessOfCaseAndWhitespace() {
        Recipe first = recipe(
                ingredient(" Gulrøtter ", 300, "g")
        );
        Recipe second = recipe(
                ingredient("gulrøtter", 200, "G")
        );

        List<RecipeIngredient> result =
                service.merge(List.of(first, second));

        assertEquals(1, result.size());
        assertEquals("Gulrøtter", result.getFirst().searchTerm());
        assertEquals(500.0, result.getFirst().amount(), 0.001);
    }

    @Test
    void keepsDifferentUnitsSeparate() {
        Recipe first = recipe(
                ingredient("melk", 1, "l")
        );
        Recipe second = recipe(
                ingredient("melk", 500, "ml")
        );

        List<RecipeIngredient> result =
                service.merge(List.of(first, second));

        assertEquals(2, result.size());
    }

    @Test
    void ignoresBlankAndNullIngredients() {
        Recipe recipe = recipeWithIngredients(
                Arrays.asList(
                        ingredient(" ", 100, "g"),
                        null,
                        ingredient("poteter", 400, "g")
                )
        );

        List<RecipeIngredient> result =
                service.merge(List.of(recipe));

        assertEquals(1, result.size());
        assertEquals("poteter", result.getFirst().searchTerm());
    }

    @Test
    void ignoresNullRecipeAndRecipeWithoutIngredients() {
        List<Recipe> recipes = Arrays.asList(
                null,
                recipeWithIngredients(null)
        );

        assertTrue(service.merge(recipes).isEmpty());
    }

    @Test
    void preservesOrderOfFirstAppearance() {
        Recipe first = recipeWithIngredients(List.of(
                ingredient("ris", 200, "g"),
                ingredient("løk", 1, "stk")
        ));
        Recipe second = recipe(
                ingredient("ris", 100, "g")
        );

        List<RecipeIngredient> result =
                service.merge(List.of(first, second));

        assertEquals(List.of("ris", "løk"),
                result.stream()
                        .map(RecipeIngredient::searchTerm)
                        .toList());
        assertEquals(300.0, result.getFirst().amount(), 0.001);
    }

    private RecipeIngredient ingredient(
            String term,
            double amount,
            String unit
    ) {
        return new RecipeIngredient(
                term, amount, unit, "PANTRY_OTHER"
        );
    }

    private Recipe recipe(RecipeIngredient ingredient) {
        return recipeWithIngredients(List.of(ingredient));
    }

    private Recipe recipeWithIngredients(
            List<RecipeIngredient> ingredients
    ) {
        return new Recipe(
                "Test recipe",
                2,
                30,
                400,
                15.0,
                ingredients,
                List.of("Cook the ingredients.")
        );
    }
}