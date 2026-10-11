package com.example.smartdagligvare.Dto.Kassalapp;

import java.util.List;

public record ProductApiResponse(
        List<KassalappProduct> data
) {
}