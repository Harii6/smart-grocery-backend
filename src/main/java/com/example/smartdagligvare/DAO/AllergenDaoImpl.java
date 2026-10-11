package com.example.smartdagligvare.DAO;

import com.example.smartdagligvare.Model.Allergen;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class AllergenDaoImpl implements AllergenRepository{
    private final EntityManager entityManager;

    public AllergenDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Allergen> findAll() {
        return entityManager.createQuery(
                "SELECT a FROM Allergen a ORDER BY a.displayName",
                Allergen.class
        ).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Allergen> findById(Long id) {
        return Optional.ofNullable(
                entityManager.find(Allergen.class, id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Allergen> findByCode(String code) {
        return entityManager.createQuery(
                        """
                        SELECT a
                        FROM Allergen a
                        WHERE a.code = :code
                        """,
                        Allergen.class
                )
                .setParameter("code", code)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional
    public Allergen save(Allergen allergen) {
        if (allergen.getId() == null) {
            entityManager.persist(allergen);
            return allergen;
        }

        return entityManager.merge(allergen);
    }

    @Override
    @Transactional
    public Allergen update(
            Long id,
            Allergen updatedAllergen
    ) {
        updatedAllergen.setId(id);

        return entityManager.merge(updatedAllergen);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Allergen allergen =
                entityManager.find(Allergen.class, id);

        if (allergen != null) {
            entityManager.remove(allergen);
        }
    }
}
