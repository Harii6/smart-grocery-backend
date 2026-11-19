package com.example.smartdagligvare.Controllers;

import com.example.smartdagligvare.DAO.AppUserRepository;
import com.example.smartdagligvare.Dto.UserRequest;
import com.example.smartdagligvare.Model.AppUser;
import com.example.smartdagligvare.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AppUserRepository appUserRepository;

    public UserController(
            UserService userService,
            AppUserRepository appUserRepository
    ) {
        this.userService = userService;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/me")
    public SettingsResponse getMySettings(Principal principal) {
        Long userId = getCurrentUserId(principal);

        return toResponse(userService.findById(userId));
    }

    @PutMapping("/me")
    public SettingsResponse updateMySettings(
            Principal principal,
            @Valid @RequestBody UserRequest request
    ) {
        Long userId = getCurrentUserId(principal);

        AppUser updatedUser = userService.update(userId, request);

        return toResponse(updatedUser);
    }

    /*
     * Compatibility with existing Home and Shopping pages.
     * A logged-in user may only read their own account.
     */
    @GetMapping("/{id}")
    public SettingsResponse findById(
            @PathVariable Long id,
            Principal principal
    ) {
        Long currentUserId = getCurrentUserId(principal);

        if (!currentUserId.equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another user's settings."
            );
        }

        return toResponse(userService.findById(currentUserId));
    }

    private Long getCurrentUserId(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please sign in."
            );
        }

        return appUserRepository
                .findByUsername(principal.getName())
                .map(AppUser::getId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Your account is no longer available."
                        )
                );
    }

    private SettingsResponse toResponse(AppUser user) {
        return new SettingsResponse(
                user.getId(),
                user.getUsername(),
                user.isSettingsCompleted(),
                user.getNumberOfPeople(),
                user.getWeeklyBudget(),
                user.getMaximumStores(),
                user.getLocationText(),
                user.getRadiusKm(),

                user.getPreferences().stream()
                        .map(preference -> new OptionResponse(
                                preference.getCode(),
                                preference.getDisplayName()
                        ))
                        .toList(),

                user.getAllergens().stream()
                        .map(allergen -> new OptionResponse(
                                allergen.getCode(),
                                allergen.getDisplayName()
                        ))
                        .toList()
        );
    }

    public record SettingsResponse(
            Long id,
            String username,
            boolean settingsCompleted,
            Integer numberOfPeople,
            BigDecimal weeklyBudget,
            Integer maximumStores,
            String locationText,
            Double radiusKm,
            List<OptionResponse> preferences,
            List<OptionResponse> allergens
    ) {
    }

    public record OptionResponse(
            String code,
            String displayName
    ) {
    }
}