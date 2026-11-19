package com.example.smartdagligvare.Controllers;

import com.example.smartdagligvare.DAO.AppUserRepository;
import com.example.smartdagligvare.Dto.Recipe.RecipeIngredient;
import com.example.smartdagligvare.Model.AppUser;
import com.example.smartdagligvare.Service.ShoppingListService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShoppingListControllerTest {
    private final ShoppingListService service = mock(ShoppingListService.class);
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final ShoppingListController controller = new ShoppingListController(service, users);
    private final Principal alice = () -> "alice";

    @Test
    void refusesAnotherUsersShoppingList() {
        when(users.findByUsername("alice")).thenReturn(Optional.of(user(7L)));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.create(8L, List.of(new RecipeIngredient("ris", 1, "kg", "PANTRY_OTHER")), alice));
        assertEquals(403, error.getStatusCode().value());
        verifyNoInteractions(service);
    }

    @Test
    void refusesIncompleteSettings() {
        when(users.findByUsername("alice")).thenReturn(Optional.of(user(7L)));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.create(7L, List.of(new RecipeIngredient("ris", 1, "kg", "PANTRY_OTHER")), alice));
        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(service);
    }

    private AppUser user(Long id) {
        AppUser result = new AppUser();
        result.setId(id);
        result.setUsername("alice");
        result.setWeeklyBudget(BigDecimal.TEN);
        result.setMaximumStores(2);
        return result;
    }
}
