package com.example.smartdagligvare.DAO;

import com.example.smartdagligvare.Model.ShoppingPlanSnapshot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ShoppingPlanSnapshotDaoImpl
        implements ShoppingPlanSnapshotRepository {

    private final EntityManager entityManager;

    public ShoppingPlanSnapshotDaoImpl(
            EntityManager entityManager
    ) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public ShoppingPlanSnapshot saveNew(
            ShoppingPlanSnapshot snapshot
    ) {
        entityManager.persist(snapshot);
        return snapshot;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<ShoppingPlanSnapshot> findForConfirmation(
            UUID snapshotId,
            Long userId
    ) {
        return entityManager.createQuery(
                        """
                        SELECT snapshot
                        FROM ShoppingPlanSnapshot snapshot
                        WHERE snapshot.id = :snapshotId
                          AND snapshot.userId = :userId
                        """,
                        ShoppingPlanSnapshot.class
                )
                .setParameter("snapshotId", snapshotId)
                .setParameter("userId", userId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShoppingPlanSnapshot> findConfirmedBetween(
            Long userId,
            Instant fromInclusive,
            Instant toExclusive
    ) {
        return entityManager.createQuery(
                        """
                        SELECT snapshot
                        FROM ShoppingPlanSnapshot snapshot
                        WHERE snapshot.userId = :userId
                          AND snapshot.confirmedAt IS NOT NULL
                          AND snapshot.confirmedAt >= :fromInclusive
                          AND snapshot.confirmedAt < :toExclusive
                        ORDER BY snapshot.confirmedAt ASC, snapshot.id ASC
                        """,
                        ShoppingPlanSnapshot.class
                )
                .setParameter("userId", userId)
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive)
                .getResultList();
    }
}