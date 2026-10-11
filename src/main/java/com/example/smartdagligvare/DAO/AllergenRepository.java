package com.example.smartdagligvare.DAO;




import com.example.smartdagligvare.Model.Allergen;

import java.util.List;
import java.util.Optional;

public interface AllergenRepository {


    List<Allergen> findAll();

    Optional<Allergen> findById(Long id);

    Optional<Allergen> findByCode(String code);

    Allergen save(Allergen allergen);

    Allergen update(
            Long id,
            Allergen updatedAllergen
    );


    void deleteById(Long id);

}
