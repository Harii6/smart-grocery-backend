package com.example.smartdagligvare.Dto.ShoppingList;

import java.math.BigDecimal;
import java.util.List;

public record StoreRecommendation(
        List<SelectedProduct> selections,
        List<String> storesUsed,
        BigDecimal totalPrice,
        boolean possible
) {
}