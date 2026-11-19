package com.example.smartdagligvare.Controllers;

import com.example.smartdagligvare.DAO.AppUserRepository;
import com.example.smartdagligvare.Dto.Recipe.GeneratedMealPlansResponse;
import com.example.smartdagligvare.Service.MealPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/api/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;
    private final AppUserRepository appUserRepository;

    public MealPlanController(
            MealPlanService mealPlanService,
            AppUserRepository appUserRepository
    ) {
        this.mealPlanService = mealPlanService;
        this.appUserRepository = appUserRepository;
    }

    @PostMapping("/{userId}/generate")
    public GeneratedMealPlansResponse generate(
            @PathVariable Long userId,
            Principal principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please sign in."
            );
        }

        boolean isOwner = appUserRepository
                .findByUsername(principal.getName())
                .map(user -> userId.equals(user.getId()))
                .orElse(false);

        if (!isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot generate meals for another account."
            );
        }

        return mealPlanService.generateMealPlan(userId);
    }
}