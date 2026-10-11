package com.example.smartdagligvare.DAO;

import com.example.smartdagligvare.Model.Preference;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public class PreferenceDaoImpl implements  PreferenceRepository{

    private final EntityManager entityManager;

    public PreferenceDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Preference> findAll() {
        return entityManager.createQuery(
                "SELECT p FROM Preference p ORDER BY p.displayName",
                Preference.class
        ).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Preference> findById(Long id) {
        return Optional.ofNullable(
                entityManager.find(Preference.class, id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Preference> findByCode(String code) {
        return entityManager.createQuery(
                        """
                        SELECT p
                        FROM Preference p
                        WHERE p.code = :code
                        """,
                        Preference.class
                )
                .setParameter("code", code)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional
    public Preference save(Preference preference) {
        if (preference.getId() == null) {
            entityManager.persist(preference);
            return preference;
        }

        return entityManager.merge(preference);
    }

    @Override
    @Transactional
    public Preference update(
            Long id,
            Preference updatedPreference
    ) {
        updatedPreference.setId(id);

        return entityManager.merge(updatedPreference);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Preference preference =
                entityManager.find(Preference.class, id);

        if (preference != null) {
            entityManager.remove(preference);
        }
    }
}
