package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.DAO.ShoppingPlanSnapshotRepository;
import com.example.smartdagligvare.Dto.ShoppingList.ShoppingListResponse;
import com.example.smartdagligvare.Dto.ShoppingList.StoreRecommendation;
import com.example.smartdagligvare.Model.ShoppingPlanSnapshot;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ShoppingPlanSnapshotServiceTest {
    private final ShoppingPlanSnapshotRepository repository = mock(ShoppingPlanSnapshotRepository.class);
    private final ShoppingPlanSnapshotService service =
            new ShoppingPlanSnapshotService(repository, new ObjectMapper());

    @Test
    void missingIngredientsPreventSavingsComparison() {
        ShoppingListResponse list = new ShoppingListResponse(7L, BigDecimal.TEN,
                List.of(), List.of("ris"), null);

        service.saveGeneratedList(list, null, false);

        var captured = org.mockito.ArgumentCaptor.forClass(ShoppingPlanSnapshot.class);
        verify(repository).saveNew(captured.capture());
        assertFalse(captured.getValue().isComparisonAvailable());
        assertNull(captured.getValue().getEstimatedSavings());
    }

    @Test
    void noProductsPreventSavingsComparison() {
        ShoppingListResponse list = new ShoppingListResponse(7L, BigDecimal.TEN,
                List.of(), List.of(), null);
        service.saveGeneratedList(list, null, true);

        var captured = org.mockito.ArgumentCaptor.forClass(ShoppingPlanSnapshot.class);
        verify(repository).saveNew(captured.capture());
        assertFalse(captured.getValue().isComparisonAvailable());
    }
}
