package com.example.smartdagligvare.Dto.Kassalapp;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record KassalappProduct(
        Long id,
        String name,
        String brand,
        String vendor,
        String ean,

        @JsonProperty("current_price")
        BigDecimal currentPrice,

        @JsonProperty("current_unit_price")
        BigDecimal currentUnitPrice,

        @JsonProperty("weight")
        BigDecimal weight,

        @JsonProperty("weight_unit")
        String weightUnit,

        ProductStore store,

        List<KassalappAllergen> allergens
) {
}