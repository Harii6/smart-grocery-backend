package com.example.smartdagligvare.Model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, length = 50)
    private String username;

    @JsonIgnore
    @Column(name = "password_hash", length = 255)
    private String passwordHash;


    @Column(name = "number_of_people")
    private Integer numberOfPeople;

    @Column(
            name = "weekly_budget",
            precision = 10,
            scale = 2
    )
    private BigDecimal weeklyBudget;

    @Column(name = "maximum_stores")
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

    @Column(name = "settings_completed", nullable = false)
    private boolean settingsCompleted = false;



    @Column(
            name = "location_text",
            length = 150
    )
    private String locationText;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "radius_km")
    private Double radiusKm;

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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getRadiusKm() {
        return radiusKm;
    }

    public void setRadiusKm(Double radiusKm) {
        this.radiusKm = radiusKm;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isSettingsCompleted() {
        return settingsCompleted;
    }

    public void setSettingsCompleted(boolean settingsCompleted) {
        this.settingsCompleted = settingsCompleted;
    }

}