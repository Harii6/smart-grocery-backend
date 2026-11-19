package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.Client.RecipeClient;
import com.example.smartdagligvare.Dto.Recipe.GeneratedMealPlansResponse;
import com.example.smartdagligvare.Dto.Recipe.MealPlanResponse;
import com.example.smartdagligvare.Dto.Recipe.Recipe;
import com.example.smartdagligvare.Dto.Recipe.RecipeIngredient;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListResponse;
import com.example.smartdagligvare.Model.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class MealPlanService {

    private static final Logger log = LoggerFactory.getLogger(MealPlanService.class);

    private final RecipeClient recipeClient;
    private final UserService userService;
    private final ShoppingListService shoppingListService;
    private final IngredientMergeService ingredientMergeService;

    public MealPlanService(
            RecipeClient recipeClient,
            UserService userService,
            ShoppingListService shoppingListService, IngredientMergeService ingredientMergeService
    ) {
        this.recipeClient = recipeClient;
        this.userService = userService;
        this.shoppingListService = shoppingListService;
        this.ingredientMergeService = ingredientMergeService;
    }

    public GeneratedMealPlansResponse generateMealPlan(
            Long userId
    ) {
        long totalStarted = System.nanoTime();
        long stageStarted = System.nanoTime();
        AppUser user = userService.findById(userId);
        log.info("meal_plan user_lookup_ms={} user_id={}", elapsedMs(stageStarted), userId);

        List<String> preferenceCodes = user.getPreferences()
                .stream()
                .map(preference -> preference.getCode())
                .toList();

        List<String> allergenCodes = user.getAllergens()
                .stream()
                .map(allergen -> allergen.getCode())
                .toList();

        stageStarted = System.nanoTime();
        MealPlanResponse generatedRecipes;
        try {
            generatedRecipes = recipeClient.generateRecipes(
                    user.getNumberOfPeople(),
                    preferenceCodes,
                    allergenCodes
            );
        } finally {
            log.info("meal_plan ai_ms={} user_id={}", elapsedMs(stageStarted), userId);
        }

        if (generatedRecipes == null ||
                generatedRecipes.recipes() == null ||
                generatedRecipes.recipes().isEmpty()) {
            throw new RuntimeException(
                    "The AI did not return any recipes"
            );
        }

        List<Recipe> recipes =
                generatedRecipes.recipes();

        stageStarted = System.nanoTime();
        List<RecipeIngredient> combinedIngredients =
                ingredientMergeService.merge(recipes);
        log.info("meal_plan merge_ms={} ingredient_count={} user_id={}",
                elapsedMs(stageStarted), combinedIngredients.size(), userId);

        stageStarted = System.nanoTime();
        ShoppingListResponse shoppingList =
                shoppingListService.createShoppingList(
                        userId,
                        combinedIngredients
                );
        log.info("meal_plan shopping_ms={} total_ms={} user_id={}",
                elapsedMs(stageStarted), elapsedMs(totalStarted), userId);

        return new GeneratedMealPlansResponse(
                user.getId(),
                user.getWeeklyBudget(),
                recipes,
                shoppingList
        );
    }

    private static double elapsedMs(long started) {
        return (System.nanoTime() - started) / 1_000_000.0;
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
                                    unit,
                                    ingredient.category()
                            )
                    );
                } else {
                    mergedIngredients.put(
                            key,
                            new RecipeIngredient(
                                    existingIngredient.searchTerm(),
                                    existingIngredient.amount()
                                            + ingredient.amount(),
                                    existingIngredient.unit(),
                                    existingIngredient.category()
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
