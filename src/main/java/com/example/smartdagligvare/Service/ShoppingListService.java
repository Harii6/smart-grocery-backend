package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.Dto.Kassalapp.KassalappProduct;
import com.example.smartdagligvare.Dto.Recipe.RecipeIngredient;
import com.example.smartdagligvare.Dto.ShoppingList.ProductOption;
import com.example.smartdagligvare.Dto.ShoppingList.SelectedProduct;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListCalculationRequest;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListCalculationResponse;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListItem;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListResponse;
import com.example.smartdagligvare.Model.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.smartdagligvare.Dto.ShoppingList.StoreRecommendation;
import java.util.Comparator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class ShoppingListService {

    private static final Pattern PACKAGE_MEASUREMENT_PATTERN =
            Pattern.compile(
                    "(\\d+(?:[.,]\\d+)?)\\s*(kg|g|ml|cl|dl|l)\\b",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern PACKAGE_COUNT_PATTERN =
            Pattern.compile(
                    "(\\d+)\\s*(pk|stk)\\b",
                    Pattern.CASE_INSENSITIVE
            );

    private final UserService userService;
    private final ProductRecommendationService recommendationService;

    public ShoppingListService(
            UserService userService,
            ProductRecommendationService recommendationService
    ) {
        this.userService = userService;
        this.recommendationService = recommendationService;
    }

    public ShoppingListResponse createShoppingList(
            Long userId,
            List<RecipeIngredient> ingredients
    ) {
        AppUser user = userService.findById(userId);

        List<ShoppingListItem> items = new ArrayList<>();
        List<String> missingIngredients = new ArrayList<>();

        for (RecipeIngredient ingredient : ingredients) {
            List<KassalappProduct> products =
                    recommendationService.findProductsForUser(
                            userId,
                            ingredient.searchTerm()
                    );

            if (products.isEmpty()) {
                missingIngredients.add(
                        ingredient.searchTerm()
                );
                continue;
            }

            List<ProductOption> options = products.stream()
                    .limit(5)
                    .map(product -> {
                        PackageInfo packageInfo =
                                resolvePackageInfo(product);

                        int requiredPackages =
                                calculateRequiredPackages(
                                        ingredient.amount(),
                                        ingredient.unit(),
                                        packageInfo.size(),
                                        packageInfo.unit()
                                );

                        BigDecimal calculatedPrice =
                                product.currentPrice()
                                        .multiply(
                                                BigDecimal.valueOf(
                                                        requiredPackages
                                                )
                                        )
                                        .setScale(
                                                2,
                                                RoundingMode.HALF_UP
                                        );

                        return new ProductOption(
                                product.id(),
                                product.name(),
                                product.currentPrice(),
                                product.store().name(),
                                packageInfo.size(),
                                packageInfo.unit(),
                                requiredPackages,
                                calculatedPrice
                        );
                    })
                    .toList();

            items.add(
                    new ShoppingListItem(
                            ingredient.searchTerm(),
                            ingredient.amount(),
                            ingredient.unit(),
                            options
                    )
            );
        }
        StoreRecommendation recommendation;

        if (missingIngredients.isEmpty()) {
            recommendation = recommendStoreCombination(
                    items,
                    user.getMaximumStores()
            );
        } else {
            recommendation = new StoreRecommendation(
                    List.of(),
                    List.of(),
                    BigDecimal.ZERO,
                    false
            );
        }

        return new ShoppingListResponse(
                userId,
                user.getWeeklyBudget(),
                items,
                missingIngredients,
                recommendation
        );
    }

    private StoreRecommendation recommendStoreCombination(
            List<ShoppingListItem> items,
            int maximumStores
    ) {
        if (items.isEmpty() || maximumStores < 1) {
            return new StoreRecommendation(
                    List.of(),
                    List.of(),
                    BigDecimal.ZERO,
                    false
            );
        }

        List<String> availableStores = items.stream()
                .flatMap(item -> item.options().stream())
                .map(ProductOption::store)
                .filter(store ->
                        store != null && !store.isBlank()
                )
                .distinct()
                .sorted()
                .toList();

        List<List<String>> combinations =
                new ArrayList<>();

        int largestCombination =
                Math.min(
                        maximumStores,
                        availableStores.size()
                );

        for (int size = 1;
             size <= largestCombination;
             size++) {

            buildStoreCombinations(
                    availableStores,
                    size,
                    0,
                    new ArrayList<>(),
                    combinations
            );
        }

        StoreRecommendation bestRecommendation = null;

        for (List<String> allowedStores : combinations) {
            List<SelectedProduct> selections =
                    new ArrayList<>();

            BigDecimal totalPrice = BigDecimal.ZERO;
            boolean suppliesEveryIngredient = true;

            for (ShoppingListItem item : items) {
                ProductOption cheapestOption =
                        item.options()
                                .stream()
                                .filter(option ->
                                        allowedStores.contains(
                                                option.store()
                                        )
                                )
                                .min(Comparator.comparing(
                                        ProductOption::calculatedPrice
                                ))
                                .orElse(null);

                if (cheapestOption == null) {
                    suppliesEveryIngredient = false;
                    break;
                }

                selections.add(
                        new SelectedProduct(
                                item.ingredient(),
                                cheapestOption.productId(),
                                cheapestOption.productName(),
                                cheapestOption.price(),
                                cheapestOption.store(),
                                cheapestOption.requiredPackages()
                        )
                );

                totalPrice = totalPrice.add(
                        cheapestOption.calculatedPrice()
                );
            }

            if (!suppliesEveryIngredient) {
                continue;
            }

            totalPrice = totalPrice.setScale(
                    2,
                    RoundingMode.HALF_UP
            );

            List<String> storesUsed = selections.stream()
                    .map(SelectedProduct::store)
                    .distinct()
                    .sorted()
                    .toList();

            StoreRecommendation candidate =
                    new StoreRecommendation(
                            selections,
                            storesUsed,
                            totalPrice,
                            true
                    );

            boolean cheaper =
                    bestRecommendation == null ||
                            candidate.totalPrice().compareTo(
                                    bestRecommendation.totalPrice()
                            ) < 0;

            boolean samePriceButFewerStores =
                    bestRecommendation != null &&
                            candidate.totalPrice().compareTo(
                                    bestRecommendation.totalPrice()
                            ) == 0 &&
                            candidate.storesUsed().size() <
                                    bestRecommendation
                                            .storesUsed()
                                            .size();

            if (cheaper || samePriceButFewerStores) {
                bestRecommendation = candidate;
            }
        }

        if (bestRecommendation == null) {
            return new StoreRecommendation(
                    List.of(),
                    List.of(),
                    BigDecimal.ZERO,
                    false
            );
        }

        return bestRecommendation;
    }

    private void buildStoreCombinations(
            List<String> stores,
            int requiredSize,
            int startIndex,
            List<String> currentCombination,
            List<List<String>> combinations
    ) {
        if (currentCombination.size() == requiredSize) {
            combinations.add(
                    List.copyOf(currentCombination)
            );
            return;
        }

        for (int index = startIndex;
             index < stores.size();
             index++) {

            currentCombination.add(
                    stores.get(index)
            );

            buildStoreCombinations(
                    stores,
                    requiredSize,
                    index + 1,
                    currentCombination,
                    combinations
            );

            currentCombination.remove(
                    currentCombination.size() - 1
            );
        }
    }





    private PackageInfo resolvePackageInfo(
            KassalappProduct product
    ) {
        if (product.weight() != null &&
                product.weight().signum() > 0 &&
                product.weightUnit() != null &&
                !product.weightUnit().isBlank()) {

            return new PackageInfo(
                    product.weight(),
                    product.weightUnit()
            );
        }

        return parsePackageInfoFromName(
                product.name()
        );
    }

    private PackageInfo parsePackageInfoFromName(
            String productName
    ) {
        if (productName == null ||
                productName.isBlank()) {
            return new PackageInfo(
                    BigDecimal.ZERO,
                    ""
            );
        }

        Matcher measurementMatcher =
                PACKAGE_MEASUREMENT_PATTERN.matcher(
                        productName
                );

        PackageInfo measurementResult = null;

        while (measurementMatcher.find()) {
            String number = measurementMatcher
                    .group(1)
                    .replace(",", ".");

            String unit = measurementMatcher
                    .group(2)
                    .toLowerCase();

            measurementResult = new PackageInfo(
                    new BigDecimal(number),
                    unit
            );
        }

        if (measurementResult != null) {
            return measurementResult;
        }

        Matcher countMatcher =
                PACKAGE_COUNT_PATTERN.matcher(
                        productName
                );

        PackageInfo countResult = null;

        while (countMatcher.find()) {
            countResult = new PackageInfo(
                    new BigDecimal(
                            countMatcher.group(1)
                    ),
                    "piece"
            );
        }

        if (countResult != null) {
            return countResult;
        }

        return new PackageInfo(
                BigDecimal.ZERO,
                ""
        );
    }

    private int calculateRequiredPackages(
            double requiredAmount,
            String requiredUnit,
            BigDecimal packageSize,
            String packageUnit
    ) {
        if (requiredAmount <= 0) {
            return 1;
        }

        String normalizedRequiredUnit =
                normalizeUnit(requiredUnit);

        if (normalizedRequiredUnit.equals("package")) {
            return Math.max(
                    1,
                    (int) Math.ceil(requiredAmount)
            );
        }

        if (packageSize == null ||
                packageSize.signum() <= 0) {
            return 1;
        }

        BaseAmount required =
                convertToBaseUnit(
                        requiredAmount,
                        normalizedRequiredUnit
                );

        BaseAmount productPackage =
                convertToBaseUnit(
                        packageSize.doubleValue(),
                        normalizeUnit(packageUnit)
                );

        if (required == null ||
                productPackage == null ||
                !required.category()
                        .equals(productPackage.category())) {
            return 1;
        }

        int packages = (int) Math.ceil(
                required.amount() /
                        productPackage.amount()
        );

        return Math.max(1, packages);
    }

    private BaseAmount convertToBaseUnit(
            double amount,
            String unit
    ) {
        return switch (unit) {
            case "g" ->
                    new BaseAmount(amount, "weight");

            case "hg" ->
                    new BaseAmount(
                            amount * 100,
                            "weight"
                    );

            case "kg" ->
                    new BaseAmount(
                            amount * 1000,
                            "weight"
                    );

            case "ml" ->
                    new BaseAmount(amount, "volume");

            case "cl" ->
                    new BaseAmount(
                            amount * 10,
                            "volume"
                    );

            case "dl" ->
                    new BaseAmount(
                            amount * 100,
                            "volume"
                    );

            case "l" ->
                    new BaseAmount(
                            amount * 1000,
                            "volume"
                    );

            case "tablespoon" ->
                    new BaseAmount(
                            amount * 15,
                            "volume"
                    );

            case "teaspoon" ->
                    new BaseAmount(
                            amount * 5,
                            "volume"
                    );

            case "piece" ->
                    new BaseAmount(amount, "piece");

            default -> null;
        };
    }

    private String normalizeUnit(String unit) {
        if (unit == null) {
            return "";
        }

        return switch (
                unit.trim().toLowerCase()
                ) {
            case "gram", "grams" ->
                    "g";

            case "kilogram", "kilograms" ->
                    "kg";

            case "liter", "litre" ->
                    "l";

            case "stk", "st",
                 "piece", "pieces" ->
                    "piece";

            case "ss", "tbsp",
                 "spiseskje" ->
                    "tablespoon";

            case "ts", "tsk",
                 "tsp", "teskje" ->
                    "teaspoon";

            case "boks", "bokser",
                 "pakke", "pakker",
                 "pose", "poser" ->
                    "package";

            default ->
                    unit.trim().toLowerCase();
        };
    }

    public ShoppingListCalculationResponse calculateTotal(
            ShoppingListCalculationRequest request
    ) {
        if (request == null ||
                request.userId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "userId is required"
            );
        }

        if (request.selections() == null ||
                request.selections().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one product must be selected"
            );
        }

        AppUser user =
                userService.findById(
                        request.userId()
                );

        BigDecimal totalPrice =
                BigDecimal.ZERO;

        for (SelectedProduct selection :
                request.selections()) {

            if (selection == null ||
                    selection.price() == null ||
                    selection.price().signum() < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Every selection must have a valid price"
                );
            }

            if (selection.quantity() < 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product quantity must be at least 1"
                );
            }

            BigDecimal productTotal =
                    selection.price()
                            .multiply(
                                    BigDecimal.valueOf(
                                            selection.quantity()
                                    )
                            );

            totalPrice =
                    totalPrice.add(productTotal);
        }

        totalPrice = totalPrice.setScale(
                2,
                RoundingMode.HALF_UP
        );

        BigDecimal weeklyBudget =
                user.getWeeklyBudget();

        BigDecimal remainingBudget =
                weeklyBudget
                        .subtract(totalPrice)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        boolean withinBudget =
                totalPrice.compareTo(
                        weeklyBudget
                ) <= 0;

        List<String> storesUsed =
                request.selections()
                        .stream()
                        .map(SelectedProduct::store)
                        .filter(store ->
                                store != null &&
                                        !store.isBlank()
                        )
                        .distinct()
                        .sorted()
                        .toList();

        boolean withinStoreLimit =
                storesUsed.size() <=
                        user.getMaximumStores();

        return new ShoppingListCalculationResponse(
                user.getId(),
                request.selections(),
                weeklyBudget,
                totalPrice,
                remainingBudget,
                withinBudget,
                storesUsed,
                user.getMaximumStores(),
                withinStoreLimit
        );
    }

    private record BaseAmount(
            double amount,
            String category
    ) {
    }

    private record PackageInfo(
            BigDecimal size,
            String unit
    ) {
    }
}