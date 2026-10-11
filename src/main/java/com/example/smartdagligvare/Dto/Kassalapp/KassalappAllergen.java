package com.example.smartdagligvare.Dto.Kassalapp;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KassalappAllergen(
        String code,

        @JsonProperty("display_name")
        String displayName,

        String contains
) {
}