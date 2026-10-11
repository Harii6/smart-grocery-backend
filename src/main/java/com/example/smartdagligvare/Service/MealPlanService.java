package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.Client.RecipeClient;
import com.example.smartdagligvare.Dto.Recipe.GeneratedMealPlansResponse;
import com.example.smartdagligvare.Dto.Recipe.MealPlanResponse;
import com.example.smartdagligvare.Dto.Recipe.Recipe;
import com.example.smartdagligvare.Dto.Recipe.RecipeIngredient;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListResponse;
import com.example.smartdagligvare.Model.AppUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class MealPlanService {

    private final RecipeClient recipeClient;
    private final UserService userService;
    private final ShoppingListService shoppingListService;

    public MealPlanService(
            RecipeClient recipeClient,
            UserService userService,
            ShoppingListService shoppingListService
    ) {
        this.recipeClient = recipeClient;
        this.userService = userService;
        this.shoppingListService = shoppingListService;
    }

    public GeneratedMealPlansResponse generateMealPlan(
            Long userId
    ) {
        AppUser user = userService.findById(userId);

        List<String> preferenceCodes = user.getPreferences()
                .stream()
                .map(preference -> preference.getCode())
                .toList();

        List<String> allergenCodes = user.getAllergens()
                .stream()
                .map(allergen -> allergen.getCode())
                .toList();

        MealPlanResponse generatedRecipes =
                recipeClient.generateRecipes(
                        user.getNumberOfPeople(),
                        preferenceCodes,
                        allergenCodes
                );

        if (generatedRecipes == null ||
                generatedRecipes.recipes() == null ||
                generatedRecipes.recipes().isEmpty()) {
            throw new RuntimeException(
                    "The AI did not return any recipes"
            );
        }

        List<Recipe> recipes =
                generatedRecipes.recipes();

        List<RecipeIngredient> combinedIngredients =
                mergeIngredients(recipes);

        ShoppingListResponse shoppingList =
                shoppingListService.createShoppingList(
                        userId,
                        combinedIngredients
                );

        return new GeneratedMealPlansResponse(
                user.getId(),
                user.getWeeklyBudget(),
                recipes,
                shoppingList
        );
    }

    private List<RecipeIngredient> mergeIngredients(
            List<Recipe> recipes
    ) {
        Map<String, RecipeIngredient> mergedIngredients =
                new LinkedHashMap<>();

        for (Recipe recipe : recipes) {
            if (recipe == null ||
                    recipe.ingredients() == null) {
                continue;
            }

            for (RecipeIngredient ingredient :
                    recipe.ingredients()) {

                if (ingredient == null ||
                        ingredient.searchTerm() == null ||
                        ingredient.searchTerm().isBlank()) {
                    continue;
                }

                String searchTerm =
                        ingredient.searchTerm().trim();

                String unit = ingredient.unit() == null
                        ? ""
                        : ingredient.unit().trim();

                String key =
                        searchTerm.toLowerCase(Locale.ROOT)
                                + "|"
                                + unit.toLowerCase(Locale.ROOT);

                RecipeIngredient existingIngredient =
                        mergedIngredients.get(key);

                if (existingIngredient == null) {
                    mergedIngredients.put(
                            key,
                            new RecipeIngredient(
                                    searchTerm,
                                    ingredient.amount(),
                                    unit
                            )
                    );
                } else {
                    mergedIngredients.put(
                            key,
                            new RecipeIngredient(
                                    existingIngredient.searchTerm(),
                                    existingIngredient.amount()
                                            + ingredient.amount(),
                                    existingIngredient.unit()
                            )
                    );
                }
            }
        }

        return new ArrayList<>(
                mergedIngredients.values()
        );
    }
}