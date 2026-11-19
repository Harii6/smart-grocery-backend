package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.Client.RecipeClient;
import com.example.smartdagligvare.Dto.Recipe.*;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListResponse;
import com.example.smartdagligvare.Model.AppUser;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MealPlanServiceTest {
    private final RecipeClient ai = mock(RecipeClient.class);
    private final UserService users = mock(UserService.class);
    private final ShoppingListService shopping = mock(ShoppingListService.class);
    private final IngredientMergeService merger = mock(IngredientMergeService.class);
    private final MealPlanService service = new MealPlanService(ai, users, shopping, merger);

    @Test
    void passesMergedIngredientsToShoppingList() {
        AppUser user = new AppUser();
        user.setId(7L);
        user.setNumberOfPeople(2);
        user.setWeeklyBudget(new BigDecimal("1000.00"));
        when(users.findById(7L)).thenReturn(user);
        RecipeIngredient rice = new RecipeIngredient("ris", 200, "g", "PANTRY_OTHER");
        Recipe recipe = new Recipe("Ris", 2, 20, 400, 12, List.of(rice), List.of("Kok."));
        when(ai.generateRecipes(2, List.of(), List.of())).thenReturn(new MealPlanResponse(List.of(recipe)));
        when(merger.merge(List.of(recipe))).thenReturn(List.of(rice));
        ShoppingListResponse list = new ShoppingListResponse(7L, user.getWeeklyBudget(), List.of(), List.of(), null);
        when(shopping.createShoppingList(7L, List.of(rice))).thenReturn(list);

        GeneratedMealPlansResponse result = service.generateMealPlan(7L);

        assertSame(list, result.shoppingList());
        verify(merger).merge(List.of(recipe));
        verify(shopping).createShoppingList(7L, List.of(rice));
    }

    @Test
    void emptyAiResponseDoesNotSearchProducts() {
        AppUser user = new AppUser();
        user.setNumberOfPeople(2);
        when(users.findById(7L)).thenReturn(user);
        when(ai.generateRecipes(anyInt(), anyList(), anyList())).thenReturn(new MealPlanResponse(List.of()));

        assertThrows(RuntimeException.class, () -> service.generateMealPlan(7L));
        verifyNoInteractions(merger, shopping);
    }
}
