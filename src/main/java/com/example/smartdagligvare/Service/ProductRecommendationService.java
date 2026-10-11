package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.Dto.Kassalapp.KassalappProduct;
import com.example.smartdagligvare.Model.AppUser;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductRecommendationService {

    private final UserService userService;
    private final ProductSearchService productSearchService;

    public ProductRecommendationService(
            UserService userService,
            ProductSearchService productSearchService
    ) {
        this.userService = userService;
        this.productSearchService = productSearchService;
    }

    public List<KassalappProduct> findProductsForUser(
            Long userId,
            String query
    ) {
        AppUser user = userService.findById(userId);

        Set<String> userAllergenCodes = user.getAllergens()
                .stream()
                .map(allergen -> allergen.getCode().toLowerCase())
                .collect(Collectors.toSet());

        return productSearchService.searchProducts(query)
                .stream()
                .filter(product ->
                        product.currentPrice() != null
                                && product.store() != null
                                && product.store().name() != null
                )
                .filter(product ->
                        isSafeForUser(
                                product,
                                userAllergenCodes
                        )
                ).filter(product ->
                        productNameMatches(query, product.name())
                ).toList();
    }

    private boolean productNameMatches(
            String query,
            String productName
    ) {
        List<String> queryWords = words(query);
        List<String> productWords = words(productName);

        if (queryWords.isEmpty() || productWords.isEmpty()) {
            return false;
        }

        boolean firstWordIsRelevant =
                queryWords.contains(productWords.get(0));

        boolean containsEveryQueryWord =
                queryWords.stream()
                        .allMatch(productWords::contains);

        return firstWordIsRelevant && containsEveryQueryWord;
    }

    private List<String> words(String value) {
        return Arrays.stream(
                        value.toLowerCase(Locale.ROOT)
                                .split("[^\\p{L}\\p{N}]+")
                )
                .filter(word -> !word.isBlank())
                .toList();
    }

    private boolean isSafeForUser(
            KassalappProduct product,
            Set<String> userAllergenCodes
    ) {
        if (product.allergens() == null || product.allergens().isEmpty()) {
            return false;
        }

        return userAllergenCodes.stream()
                .allMatch(userAllergenCode ->
                        product.allergens().stream()
                                .anyMatch(productAllergen ->
                                        userAllergenCode.equalsIgnoreCase(
                                                productAllergen.code()
                                        )
                                                &&
                                                "NO".equalsIgnoreCase(
                                                        productAllergen.contains()
                                                )
                                )
                );
    }
}