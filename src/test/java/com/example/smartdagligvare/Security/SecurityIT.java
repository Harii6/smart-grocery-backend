package com.example.smartdagligvare.Security;

import com.example.smartdagligvare.Controllers.MealPlanController;
import com.example.smartdagligvare.DAO.AppUserRepository;
import com.example.smartdagligvare.Model.AppUser;
import com.example.smartdagligvare.Service.MealPlanService;
import com.example.smartdagligvare.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MealPlanController.class)
@Import(SecurityConfig.class)
class SecurityIT {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MealPlanService mealPlanService;

    @MockitoBean
    private AppUserRepository appUserRepository;

    private void aliceHasAccountSeven() {
        AppUser alice = new AppUser();
        alice.setId(7L);
        when(appUserRepository.findByUsername("alice"))
                .thenReturn(Optional.of(alice));
    }

    @Test
    void guestCannotGenerateMeals() throws Exception {
        mvc.perform(post("/api/meal-plans/7/generate").with(csrf()))
                .andExpect(status().is3xxRedirection());
        verifyNoInteractions(mealPlanService);
    }

    @Test
    void signedInUserWithoutCsrfCannotGenerateMeals() throws Exception {
        mvc.perform(post("/api/meal-plans/7/generate")
                        .with(user("alice")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(mealPlanService);
    }

    @Test
    void signedInUserWithCsrfCanReachGeneration() throws Exception {
        aliceHasAccountSeven();

        mvc.perform(post("/api/meal-plans/7/generate")
                        .with(user("alice"))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void signedInUserCannotGenerateForAnotherAccount() throws Exception {
        aliceHasAccountSeven();

        mvc.perform(post("/api/meal-plans/8/generate")
                        .with(user("alice"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(mealPlanService);
    }
}