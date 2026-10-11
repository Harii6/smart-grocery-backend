package com.example.smartdagligvare.Model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "number_of_people", nullable = false)
    private Integer numberOfPeople;

    @Column(
            name = "weekly_budget",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal weeklyBudget;

    @Column(name = "maximum_stores", nullable = false)
    private Integer maximumStores;

    @ManyToMany
    @JoinTable(
            name = "user_preference",
            joinColumns =
            @JoinColumn(name = "user_id"),
            inverseJoinColumns =
            @JoinColumn(name = "preference_id")
    )
    private Set<Preference> preferences = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_allergen",
            joinColumns =
            @JoinColumn(name = "user_id"),
            inverseJoinColumns =
            @JoinColumn(name = "allergen_id")
    )
    private Set<Allergen> allergens = new HashSet<>();

    public AppUser() {
    }

    public void addPreference(Preference preference) {
        preferences.add(preference);
        preference.getUsers().add(this);
    }

    public void addAllergen(Allergen allergen) {
        allergens.add(allergen);
        allergen.getUsers().add(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(Integer numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public BigDecimal getWeeklyBudget() {
        return weeklyBudget;
    }

    public void setWeeklyBudget(BigDecimal weeklyBudget) {
        this.weeklyBudget = weeklyBudget;
    }

    public Integer getMaximumStores() {
        return maximumStores;
    }

    public void setMaximumStores(Integer maximumStores) {
        this.maximumStores = maximumStores;
    }

    public Set<Preference> getPreferences() {
        return preferences;
    }

    public void setPreferences(Set<Preference> preferences) {
        this.preferences = preferences;
    }

    public Set<Allergen> getAllergens() {
        return allergens;
    }

    public void setAllergens(Set<Allergen> allergens) {
        this.allergens = allergens;
    }
}