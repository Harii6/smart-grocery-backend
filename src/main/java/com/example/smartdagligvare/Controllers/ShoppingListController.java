package com.example.smartdagligvare.Controllers;

import com.example.smartdagligvare.Dto.Recipe.RecipeIngredient;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListResponse;
import com.example.smartdagligvare.Service.ShoppingListService;
import org.springframework.web.bind.annotation.*;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListCalculationRequest;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListCalculationResponse;

import java.util.List;

@RestController
@RequestMapping("/api/shopping-lists")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    public ShoppingListController(
            ShoppingListService shoppingListService
    ) {
        this.shoppingListService = shoppingListService;
    }

    @PostMapping
    public ShoppingListResponse create(
            @RequestParam Long userId,
            @RequestBody List<RecipeIngredient> ingredients
    ) {
        return shoppingListService.createShoppingList(
                userId,
                ingredients
        );
    }

    @PostMapping("/calculate")
    public ShoppingListCalculationResponse calculate(
            @RequestBody ShoppingListCalculationRequest request
    ) {
        return shoppingListService.calculateTotal(request);
    }
}