package com.example.smartdagligvare.DAO;

import com.example.smartdagligvare.Model.ShoppingPlanSnapshot;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingPlanSnapshotRepository {

    ShoppingPlanSnapshot saveNew(
            ShoppingPlanSnapshot snapshot
    );

    Optional<ShoppingPlanSnapshot> findForConfirmation(
            UUID snapshotId,
            Long userId
    );

    List<ShoppingPlanSnapshot> findConfirmedBetween(
            Long userId,
            Instant fromInclusive,
            Instant toExclusive
    );
}