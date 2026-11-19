package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.Client.LocationClient;
import com.example.smartdagligvare.DAO.AllergenRepository;
import com.example.smartdagligvare.DAO.AppUserRepository;
import com.example.smartdagligvare.DAO.PreferenceRepository;
import com.example.smartdagligvare.Dto.UserRequest;
import com.example.smartdagligvare.Model.Allergen;
import com.example.smartdagligvare.Model.AppUser;
import com.example.smartdagligvare.Model.Preference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final AppUserRepository appUserDao;
    private final PreferenceRepository preferenceDao;
    private final AllergenRepository allergenDao;
    private final LocationClient locationClient;

    public UserServiceImpl(AppUserRepository appUserDao, PreferenceRepository preferenceDao, AllergenRepository allergenDao, LocationClient locationClient) {
        this.appUserDao = appUserDao;
        this.preferenceDao = preferenceDao;
        this.allergenDao = allergenDao;
        this.locationClient = locationClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppUser> findAll() {
        return appUserDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public AppUser findById(Long id) {
        return appUserDao.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found with ID: " + id
                        )
                );
    }



    @Override
    @Transactional
    public AppUser save(UserRequest request) {




        AppUser user = new AppUser();

        user.setNumberOfPeople(
                request.numberOfPeople()
        );
        user.setWeeklyBudget(
                request.weeklyBudget()
        );
        user.setMaximumStores(
                request.maximumStores()
        );

        if (request.preferenceCodes() != null) {
            for (String code : request.preferenceCodes()) {
                Preference preference = preferenceDao
                        .findByCode(code)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid preference: " + code
                                )
                        );

                user.getPreferences().add(preference);
            }
        }

        if (request.allergenCodes() != null) {
            for (String code : request.allergenCodes()) {
                Allergen allergen = allergenDao
                        .findByCode(code)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid allergen: " + code
                                )
                        );

                user.getAllergens().add(allergen);
            }
        }

        applyLocation(user, request);
        user.setSettingsCompleted(true);

        return appUserDao.save(user);
    }



    @Override
    @Transactional
    public void deleteById(Long id) {
        AppUser user = findById(id);
        appUserDao.deleteById(user.getId());
    }

    @Override
    @Transactional
    public AppUser update(
            Long id,
            UserRequest request
    ) {
        AppUser user = findById(id);

        user.setNumberOfPeople(
                request.numberOfPeople()
        );
        user.setWeeklyBudget(
                request.weeklyBudget()
        );
        user.setMaximumStores(
                request.maximumStores()
        );

        user.getPreferences().clear();

        if (request.preferenceCodes() != null) {
            for (String code : request.preferenceCodes()) {
                Preference preference = preferenceDao
                        .findByCode(code)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid preference: " + code
                                )
                        );

                user.getPreferences().add(preference);
            }
        }

        user.getAllergens().clear();

        if (request.allergenCodes() != null) {
            for (String code : request.allergenCodes()) {
                Allergen allergen = allergenDao
                        .findByCode(code)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid allergen: " + code
                                )
                        );

                user.getAllergens().add(allergen);
            }
        }

        applyLocation(user, request);

        return appUserDao.save(user);
    }

    private void applyLocation(
            AppUser user,
            UserRequest request
    ) {
        if (
                request.locationText() == null ||
                        request.locationText().isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Location is required."
            );
        }

        if (
                        request.radiusKm() == null ||
                        request.radiusKm() < 1 ||
                        request.radiusKm() > 50
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Radius must be between 1 and 50 km."
            );
        }

        String locationText =
                request.locationText().trim();

        boolean locationChanged =
                user.getLocationText() == null ||
                        !locationText.equalsIgnoreCase(
                                user.getLocationText()
                        ) ||
                        user.getLatitude() == null ||
                        user.getLongitude() == null;

        /*
         * Only contact the geocoding API when the location
         * changed or coordinates are missing.
         */
        if (locationChanged) {
            LocationClient.Coordinates coordinates =
                    locationClient.findCoordinates(
                            locationText
                    );

            user.setLatitude(
                    coordinates.latitude()
            );

            user.setLongitude(
                    coordinates.longitude()
            );
        }

        user.setLocationText(locationText);
        user.setRadiusKm(request.radiusKm());
    }


}
