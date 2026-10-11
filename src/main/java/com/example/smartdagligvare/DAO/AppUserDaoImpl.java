package com.example.smartdagligvare.DAO;

import com.example.smartdagligvare.Model.AppUser;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class AppUserDaoImpl implements AppUserRepository {

    private final EntityManager entityManager;

    public AppUserDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppUser> findAll() {
        return entityManager.createQuery(
                "SELECT u FROM AppUser u",
                AppUser.class
        ).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> findById(Long id) {
        AppUser user = entityManager.find(AppUser.class, id);

        return Optional.ofNullable(user);
    }


    @Override
    @Transactional
    public AppUser save(AppUser user) {
        if (user.getId() == null) {
            entityManager.persist(user);
            return user;
        }

        return entityManager.merge(user);
    }


    @Transactional
    public AppUser update(Long id,AppUser user) {
        if (user.getId() == null) {
            entityManager.persist(user);
            return user;
        }

        return entityManager.merge(user);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        AppUser user = entityManager.find(AppUser.class, id);

        if (user != null) {
            entityManager.remove(user);
        }
    }

}
