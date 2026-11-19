package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.DAO.ShoppingPlanSnapshotRepository;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListResponse;
import com.example.smartdagligvare.Dto.ShoppingList.StoreRecommendation;
import com.example.smartdagligvare.Model.ShoppingPlanSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ShoppingPlanSnapshotService {

    private final ShoppingPlanSnapshotRepository snapshotRepository;
    private final ObjectMapper objectMapper;

    public ShoppingPlanSnapshotService(
            ShoppingPlanSnapshotRepository snapshotRepository,
            ObjectMapper objectMapper
    ) {
        this.snapshotRepository = snapshotRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UUID saveGeneratedList(
            ShoppingListResponse shoppingList,
            StoreRecommendation singleStoreRecommendation,
            boolean quantitiesComparable
    ) {
        StoreRecommendation recommendation =
                shoppingList.recommendation();

        BigDecimal baselineTotal = null;
        BigDecimal recommendedTotal = null;

        String comparisonNote;

        if (shoppingList.items().isEmpty()) {
            comparisonNote =
                    "No products were found.";

        } else if (!shoppingList.missingIngredients().isEmpty()) {
            comparisonNote =
                    "Comparison unavailable: some ingredients "
                            + "have no matching products.";

        } else if (!quantitiesComparable) {
            comparisonNote =
                    "Comparison unavailable: at least one product "
                            + "has missing or incompatible package "
                            + "quantity data.";

        } else if (recommendation == null
                || !recommendation.possible()) {
            comparisonNote =
                    "Comparison unavailable: no complete basket "
                            + "was found within your store limit.";

        } else if (singleStoreRecommendation == null
                || !singleStoreRecommendation.possible()) {
            comparisonNote =
                    "Comparison unavailable: no single store "
                            + "could supply the complete basket "
                            + "among the retrieved options.";

        } else if (!hasValidTotal(recommendation)
                || !hasValidTotal(singleStoreRecommendation)) {
            comparisonNote =
                    "Comparison unavailable: a basket total "
                            + "is missing or invalid.";

        } else if (recommendation.totalPrice().compareTo(
                singleStoreRecommendation.totalPrice()
        ) > 0) {
            comparisonNote =
                    "Comparison unavailable: the recommended "
                            + "basket costs more than the "
                            + "single-store baseline.";

        } else {
            baselineTotal =
                    singleStoreRecommendation.totalPrice();

            recommendedTotal =
                    recommendation.totalPrice();

            comparisonNote =
                    "Estimated price difference against the "
                            + "cheapest complete single-store basket "
                            + "among the retrieved product options. "
                            + "Uses reported package quantities. "
                            + "Excludes travel and delivery costs. "
                            + "Not a verified purchase.";
        }

        /*
         * Store both baskets so the comparison can be inspected
         * later without fetching new prices.
         */
        SnapshotData snapshotData = new SnapshotData(
                1,
                shoppingList,
                singleStoreRecommendation,
                quantitiesComparable
        );

        String snapshotJson =
                objectMapper.writeValueAsString(snapshotData);

        ShoppingPlanSnapshot snapshot =
                new ShoppingPlanSnapshot(
                        shoppingList.userId(),
                        baselineTotal,
                        recommendedTotal,
                        comparisonNote,
                        snapshotJson
                );

        snapshotRepository.saveNew(snapshot);

        return snapshot.getId();
    }

    private boolean hasValidTotal(
            StoreRecommendation recommendation
    ) {
        return recommendation.totalPrice() != null
                && recommendation.totalPrice().signum() >= 0;
    }

    public record SnapshotData(
            int schemaVersion,
            ShoppingListResponse shoppingList,
            StoreRecommendation singleStoreRecommendation,
            boolean quantitiesComparable
    ) {
    }
}