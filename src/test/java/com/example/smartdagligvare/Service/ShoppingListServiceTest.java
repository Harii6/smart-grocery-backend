package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.Dto.Recipe.RecipeIngredient;
import com.example.smartdagligvare.Dto.ShoppingList.*;
import com.example.smartdagligvare.Model.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShoppingListServiceTest {
    private final UserService users = mock(UserService.class);
    private final ProductRecommendationService products = mock(ProductRecommendationService.class);
    private final ShoppingPlanSnapshotService snapshots = mock(ShoppingPlanSnapshotService.class);
    private final ShoppingListService service = new ShoppingListService(users, products, snapshots);

    @BeforeEach
    void setup() {
        AppUser user = new AppUser();
        user.setId(7L);
        user.setWeeklyBudget(new BigDecimal("100.00"));
        user.setMaximumStores(1);
        when(users.findById(7L)).thenReturn(user);
    }

    @Test
    void missingProductCreatesNoSnapshot() {
        RecipeIngredient rice = new RecipeIngredient("ris", 500, "g", "PANTRY_OTHER");
        when(products.findProductsForUser(7L, "ris")).thenReturn(List.of());

        ShoppingListResponse result = service.createShoppingList(7L, List.of(rice));

        assertEquals(List.of("ris"), result.missingIngredients());
        assertFalse(result.recommendation().possible());
        assertNull(result.snapshotId());
        verifyNoInteractions(snapshots);
    }

    @Test
    void calculationMultipliesPriceAndRespectsBudget() {
        SelectedProduct rice = new SelectedProduct("ris", "PANTRY_OTHER", 1L,
                "Rice", new BigDecimal("12.50"), "Meny", 3);
        ShoppingListCalculationResponse result = service.calculateTotal(
                new ShoppingListCalculationRequest(7L, List.of(rice)));

        assertEquals(0, new BigDecimal("37.50").compareTo(result.totalPrice()));
        assertEquals(0, new BigDecimal("62.50").compareTo(result.remainingBudget()));
        assertTrue(result.withinBudget());
    }

    @Test
    void calculationRejectsInvalidPriceAndQuantity() {
        SelectedProduct negative = new SelectedProduct("ris", "PANTRY_OTHER", 1L,
                "Rice", new BigDecimal("-1"), "Meny", 1);
        SelectedProduct zero = new SelectedProduct("ris", "PANTRY_OTHER", 1L,
                "Rice", BigDecimal.TEN, "Meny", 0);
        assertThrows(ResponseStatusException.class, () -> service.calculateTotal(
                new ShoppingListCalculationRequest(7L, List.of(negative))));
        assertThrows(ResponseStatusException.class, () -> service.calculateTotal(
                new ShoppingListCalculationRequest(7L, List.of(zero))));
    }

    @Test
    void twoStoresExceedOneStoreLimit() {
        SelectedProduct a = new SelectedProduct("ris", "PANTRY_OTHER", 1L, "Rice",
                new BigDecimal("70"), "Meny", 1);
        SelectedProduct b = new SelectedProduct("melk", "DAIRY", 2L, "Milk",
                new BigDecimal("40"), "Spar", 1);
        ShoppingListCalculationResponse result = service.calculateTotal(
                new ShoppingListCalculationRequest(7L, List.of(a, b)));
        assertFalse(result.withinBudget());
        assertFalse(result.withinStoreLimit());
    }
}
