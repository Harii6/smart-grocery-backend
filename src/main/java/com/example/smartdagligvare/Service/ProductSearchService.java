package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.Dto.Kassalapp.KassalappProduct;
import com.example.smartdagligvare.Dto.Kassalapp.ProductApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class ProductSearchService {

    private final RestClient restClient;

    public ProductSearchService(
            @Value("${kassalapp.base-url}") String baseUrl,
            @Value("${kassalapp.api-key}") String apiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(
                        "Authorization",
                        "Bearer " + apiKey
                )
                .build();
    }

    public List<KassalappProduct> searchProducts(String searchTerm) {

        ProductApiResponse response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/products")
                        .queryParam("search", searchTerm)
                        .queryParam("size", 100)
                        .build())
                .retrieve()
                .body(ProductApiResponse.class);

        if (response == null || response.data() == null) {
            return List.of();
        }

        return response.data();
    }
}