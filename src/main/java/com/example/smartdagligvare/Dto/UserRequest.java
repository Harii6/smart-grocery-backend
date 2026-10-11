package com.example.smartdagligvare.Dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record UserRequest(

        @Min(
                value = 1,
                message = "must be at least 1"
        )
        int numberOfPeople,

        @NotNull(
                message = "is required"
        )
        @DecimalMin(
                value = "0.01",
                message = "must be greater than 0"
        )
        BigDecimal weeklyBudget,

        @Min(
                value = 1,
                message = "must be at least 1"
        )
        int maximumStores,

        List<
                @NotBlank(
                        message = "preference code cannot be blank"
                )
                        String
                > preferenceCodes,

        List<
                @NotBlank(
                        message = "allergen code cannot be blank"
                )
                        String
                > allergenCodes
) {
}