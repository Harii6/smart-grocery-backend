package com.example.smartdagligvare.Controllers;

import com.example.smartdagligvare.DAO.AppUserRepository;
import com.example.smartdagligvare.Model.AppUser;
import com.example.smartdagligvare.Service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {
    private final UserService service = mock(UserService.class);
    private final AppUserRepository repository = mock(AppUserRepository.class);
    private final UserController controller = new UserController(service, repository);
    private final Principal principal = () -> "alice";

    @Test
    void meReturnsOnlySafeFields() {
        AppUser user = user(7L);
        user.setPasswordHash("secret-hash");
        when(repository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(service.findById(7L)).thenReturn(user);

        var result = controller.getMySettings(principal);

        assertEquals(7L, result.id());
        assertEquals("alice", result.username());
        assertEquals(10, result.getClass().getRecordComponents().length);
    }

    @Test
    void otherUsersSettingsAreForbidden() {
        when(repository.findByUsername("alice")).thenReturn(Optional.of(user(7L)));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.findById(8L, principal));

        assertEquals(403, error.getStatusCode().value());
        verifyNoInteractions(service);
    }

    private AppUser user(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername("alice");
        return user;
    }
}
