package com.example.smartdagligvare.Controllers;

import com.example.smartdagligvare.Dto.Recipe.GeneratedMealPlansResponse;
import com.example.smartdagligvare.Dto.Recipe.MealPlanResponse;
import com.example.smartdagligvare.Service.MealPlanService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @PostMapping("/{userId}/generate")
    public GeneratedMealPlansResponse generate(
            @PathVariable Long userId
    ) {
        return mealPlanService.generateMealPlan(userId);
    }


}