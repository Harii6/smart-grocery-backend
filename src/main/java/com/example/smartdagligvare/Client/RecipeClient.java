package com.example.smartdagligvare.Client;

import com.example.smartdagligvare.Dto.Recipe.MealPlanResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Component
public class RecipeClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public RecipeClient(
            @Value("${groq.base-url}") String baseUrl,
            @Value("${groq.api-key}") String apiKey,
            @Value("${groq.model}") String model,
            ObjectMapper objectMapper
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(
                        "Authorization",
                        "Bearer " + apiKey
                )
                .build();

        this.model = model;
        this.objectMapper = objectMapper;
    }

    public MealPlanResponse generateRecipes(
            int numberOfPeople,
            List<String> preferences,
            List<String> allergens
    ) {
        String prompt = createPrompt(
                numberOfPeople,
                preferences,
                allergens
        );

        Map<String, Object> requestBody = Map.of(
                "model", model,

                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "max_completion_tokens", 4096,
                "reasoning_effort", "low",
                "include_reasoning", false


        );

        JsonNode response = restClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new RuntimeException(
                    "Groq returned an empty response"
            );
        }

        String finishReason = response
                .path("choices")
                .path(0)
                .path("finish_reason")
                .asText();

        if (!"stop".equals(finishReason)) {
            throw new RuntimeException(
                    "Groq stopped early. finish_reason=" + finishReason
            );
        }

        JsonNode contentNode = response
                .path("choices")
                .path(0)
                .path("message")
                .path("content");

        if (contentNode.isMissingNode()
                || contentNode.asText().isBlank()) {
            throw new RuntimeException(
                    "Groq did not return recipe JSON"
            );
        }

        String recipeJson = contentNode.asText();

        try {
            return objectMapper.readValue(
                    recipeJson,
                    MealPlanResponse.class
            );
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Could not convert Groq response into recipes. "
                            + "Response: " + recipeJson,
                    exception
            );
        }
    }

    private String createPrompt(
            int numberOfPeople,
            List<String> preferences,
            List<String> allergens
    ) {
        return """
                Generate exactly 7 affordable dinner recipes.
                Reuse some ingredients across recipes where practical to reduce cost and food waste.

                Number of people: %d
                Food preferences: %s
                Allergens that must be avoided: %s

                Requirements:
                - Every ingredient object must represent exactly one product.
                - Never combine multiple ingredients inside one searchTerm.
                - Paprikapulver, spisskummen, salt and pepper must be separate ingredients.
                - For fresh vegetables, use specific search terms such as:
                - "gul løk", "hvitløk", "gulrøtter" and "rød paprika".
                - Return exactly one valid JSON object.
                - preparationTimeMinutes must be the estimated total preparation and cooking time as an integer.
                - caloriesPerServing must be the estimated calories for one serving as an integer.
                - proteinGramsPerServing must be the estimated grams of protein for one serving as a number.
                - Calories and protein must be calculated per serving, not for the entire recipe.
                - Use the ingredient amounts to provide reasonable nutritional estimates.
                - Do not return carbohydrate or fat information.
                - Write recipe names and instructions in Norwegian.
                - Use ingredients commonly available in Norwegian grocery stores.
                - Prefer ordinary, affordable ingredients available throughout the year.
                - Avoid niche ingredients that normally require specialist stores.
                - searchTerm must use a short and generic Norwegian product name.
                - Prefer one or two words that describe the basic ingredient.
                - Do not include brands, product ranges or marketing descriptions.
                - Do not include preparation methods, quality descriptions, packaging types or special product variants.
                - Remove unnecessary descriptive words when the basic ingredient is enough for a product search.
                - Choose search terms likely to match several products in an ordinary Norwegian grocery store.
                - Write all ingredient names in correct Norwegian Bokmål.
                - Every searchTerm must be a real standalone grocery product.
                - Do not use Danish or Swedish ingredient names.
                - Do not invent compound words or ingredient names.
                - Do not use the name of a prepared dish as an ingredient.
                - Before returning the JSON, verify that every searchTerm could reasonably appear in a Norwegian grocery-store product search.
                - Use Norwegian grocery-product names in searchTerm.
                - Respect all listed food preferences.
                - Do not include any listed allergens.
                - Prefer ordinary products available in Norwegian supermarkets.
                - Prefer inexpensive recipes.
                - Every recipe must contain servings, ingredients and instructions.
                - amount must always be a number.
                - unit must always be a string.
                - Do not return Markdown.
                - Do not surround the response with code fences.
                

                The JSON must have exactly this structure:
                
                   {
                     "recipes": [
                       {
                         "name": "Vegetarisk linsegryte",
                         "servings": 4,
                         "preparationTimeMinutes": 35,
                         "caloriesPerServing": 480,
                         "proteinGramsPerServing": 22.5,
                         "ingredients": [
                           {
                             "searchTerm": "linser",
                             "amount": 500,
                             "unit": "g"
                           },
                           {
                             "searchTerm": "tomater",
                             "amount": 2,
                             "unit": "boks"
                           }
                         ],
                         "instructions": [
                           "Kok linsene etter anvisningen på pakken.",
                           "Tilsett tomatene og la gryten småkoke."
                         ]
                       }
                     ]
                   }
                """.formatted(
                numberOfPeople,
                preferences,
                allergens
        );
    }
}