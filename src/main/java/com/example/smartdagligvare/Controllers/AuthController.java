package com.example.smartdagligvare.Controllers;

import com.example.smartdagligvare.Dto.Register.RegisterRequest;
import com.example.smartdagligvare.Dto.Register.RegisterResponse;
import com.example.smartdagligvare.Service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(
            RegistrationService registrationService
    ) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response =
                registrationService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/csrf")
    public ResponseEntity<CsrfResponse> csrf(
            CsrfToken csrfToken
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new CsrfResponse(
                        csrfToken.getHeaderName(),
                        csrfToken.getParameterName(),
                        csrfToken.getToken()
                ));
    }

    public record CsrfResponse(
            String headerName,
            String parameterName,
            String token
    ) {
    }
}