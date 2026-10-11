package com.example.smartdagligvare.Dto.ShoppingList;

import java.math.BigDecimal;

public record SelectedProduct(
        String ingredient,
        Long productId,
        String productName,
        BigDecimal price,
        String store,
        int quantity
) {
}