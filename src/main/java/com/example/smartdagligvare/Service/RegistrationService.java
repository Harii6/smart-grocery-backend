package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.DAO.AppUserRepository;
import com.example.smartdagligvare.Dto.Register.RegisterRequest;
import com.example.smartdagligvare.Dto.Register.RegisterResponse;
import com.example.smartdagligvare.Model.AppUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
@Validated
public class RegistrationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(
            @NotNull @Valid RegisterRequest request
    ) {
        String username = request.username().trim();
        String password = request.password();

        // BCrypt has a 72-byte password limit.
        // Some characters use more than one byte.
        if (
                password.getBytes(StandardCharsets.UTF_8).length > 72
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password exceeds the supported byte length. " +
                            "Use fewer multi-byte characters."
            );
        }

        if (appUserRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "That username is unavailable."
            );
        }

        AppUser user = new AppUser();

        user.setUsername(username);
        user.setPasswordHash(
                passwordEncoder.encode(password)
        );
        user.setSettingsCompleted(false);

        AppUser savedUser = appUserRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.isSettingsCompleted()
        );
    }
}