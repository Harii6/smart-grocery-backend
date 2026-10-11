package com.example.smartdagligvare.DAO;


import com.example.smartdagligvare.Model.Preference;

import java.util.List;
import java.util.Optional;
public interface PreferenceRepository {

    List<Preference> findAll();

    Optional<Preference> findById(Long id);

    Optional<Preference> findByCode(String code);

    Preference save(Preference preference);

    Preference update(
            Long id,
            Preference updatedPreference
    );

    void deleteById(Long id);



}
