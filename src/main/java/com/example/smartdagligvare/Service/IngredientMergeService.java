package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.Dto.Recipe.Recipe;
import com.example.smartdagligvare.Dto.Recipe.RecipeIngredient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class IngredientMergeService {

    public List<RecipeIngredient> merge(List<Recipe> recipes) {
        Map<String, RecipeIngredient> merged = new LinkedHashMap<>();

        for (Recipe recipe : recipes) {
            if (recipe == null || recipe.ingredients() == null) {
                continue;
            }

            for (RecipeIngredient ingredient : recipe.ingredients()) {
                if (ingredient == null
                        || ingredient.searchTerm() == null
                        || ingredient.searchTerm().isBlank()) {
                    continue;
                }

                String term = ingredient.searchTerm().trim();
                String unit = ingredient.unit() == null
                        ? ""
                        : ingredient.unit().trim();

                String key = term.toLowerCase(Locale.ROOT)
                        + "|"
                        + unit.toLowerCase(Locale.ROOT);

                RecipeIngredient existing = merged.get(key);

                if (existing == null) {
                    merged.put(key, new RecipeIngredient(
                            term,
                            ingredient.amount(),
                            unit,
                            ingredient.category()
                    ));
                } else {
                    merged.put(key, new RecipeIngredient(
                            existing.searchTerm(),
                            existing.amount() + ingredient.amount(),
                            existing.unit(),
                            existing.category()
                    ));
                }
            }
        }

        return new ArrayList<>(merged.values());
    }
}