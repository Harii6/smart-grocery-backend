package com.example.smartdagligvare.Controllers;

import com.example.smartdagligvare.Dto.Kassalapp.KassalappProduct;
import com.example.smartdagligvare.Service.ProductRecommendationService;
import com.example.smartdagligvare.Service.ProductSearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductSearchService productSearchService;
    private final ProductRecommendationService recommendationService;

    public ProductController(
            ProductSearchService productSearchService, ProductRecommendationService recommendationService
    ) {
        this.productSearchService = productSearchService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/search")
    public List<KassalappProduct> searchProducts(
            @RequestParam String query
    ) {
        return productSearchService.searchProducts(query);
    }

    @GetMapping("/recommended")
    public List<KassalappProduct> recommended(
            @RequestParam Long userId,
            @RequestParam String query
    ) {
        return recommendationService.findProductsForUser(
                userId,
                query
        );
    }
}