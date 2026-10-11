package com.example.smartdagligvare.Dto.ShoppingList;

import java.math.BigDecimal;

public record ProductOption(
        Long productId,
        String productName,
        BigDecimal price,
        String store,
        BigDecimal packageSize,
        String packageUnit,
        int requiredPackages,
        BigDecimal calculatedPrice
) {
}