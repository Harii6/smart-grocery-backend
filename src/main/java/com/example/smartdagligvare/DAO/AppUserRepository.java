package com.example.smartdagligvare.DAO;

import com.example.smartdagligvare.Model.AppUser;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository {

    List<AppUser> findAll();

    Optional<AppUser> findById(Long id);

    AppUser update(Long id,AppUser user);

    AppUser save(AppUser user);

    void deleteById(Long id);


}
