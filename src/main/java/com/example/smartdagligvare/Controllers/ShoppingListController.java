package com.example.smartdagligvare.Controllers;

import com.example.smartdagligvare.DAO.AppUserRepository;
import com.example.smartdagligvare.Dto.Recipe.RecipeIngredient;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListCalculationRequest;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListCalculationResponse;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListResponse;
import com.example.smartdagligvare.Model.AppUser;
import com.example.smartdagligvare.Service.ShoppingListService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/shopping-lists")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final AppUserRepository appUserRepository;

    public ShoppingListController(
            ShoppingListService shoppingListService,
            AppUserRepository appUserRepository
    ) {
        this.shoppingListService = shoppingListService;
        this.appUserRepository = appUserRepository;
    }

    @PostMapping
    public ShoppingListResponse create(
            @RequestParam Long userId,
            @RequestBody List<@Valid RecipeIngredient> ingredients,
            Principal principal
    ) {
        AppUser user = requireCurrentUser(userId, principal);

        if (ingredients == null || ingredients.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one ingredient is required."
            );
        }

        return shoppingListService.createShoppingList(
                user.getId(),
                ingredients
        );
    }

    @PostMapping("/calculate")
    public ShoppingListCalculationResponse calculate(
            @Valid @RequestBody ShoppingListCalculationRequest request,
            Principal principal
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A calculation request is required."
            );
        }

        requireCurrentUser(request.userId(), principal);

        return shoppingListService.calculateTotal(request);
    }

    private AppUser requireCurrentUser(
            Long requestedUserId,
            Principal principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please sign in."
            );
        }

        AppUser user = appUserRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Your account is no longer available."
                        )
                );

        if (!user.getId().equals(requestedUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot use another user's account."
            );
        }

        if (!user.isSettingsCompleted() ||
                user.getWeeklyBudget() == null ||
                user.getMaximumStores() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Complete your settings before using the shopping list."
            );
        }

        return user;
    }
}