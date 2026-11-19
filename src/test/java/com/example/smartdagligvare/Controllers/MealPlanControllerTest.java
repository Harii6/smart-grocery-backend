package com.example.smartdagligvare.Controllers;

import com.example.smartdagligvare.DAO.AppUserRepository;
import com.example.smartdagligvare.Dto.Recipe.GeneratedMealPlansResponse;
import com.example.smartdagligvare.Model.AppUser;
import com.example.smartdagligvare.Service.MealPlanService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class MealPlanControllerTest {

    @Test
    void delegatesGenerationToService() {
        MealPlanService service = mock(MealPlanService.class);
        AppUserRepository users = mock(AppUserRepository.class);

        AppUser alice = new AppUser();
        alice.setId(7L);
        when(users.findByUsername("alice"))
                .thenReturn(Optional.of(alice));

        GeneratedMealPlansResponse response =
                new GeneratedMealPlansResponse(7L, null, List.of(), null);
        when(service.generateMealPlan(7L)).thenReturn(response);

        MealPlanController controller =
                new MealPlanController(service, users);

        assertSame(
                response,
                controller.generate(7L, () -> "alice")
        );
        verify(service).generateMealPlan(7L);
    }
}