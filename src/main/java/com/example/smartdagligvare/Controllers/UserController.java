package com.example.smartdagligvare.Controllers;

import com.example.smartdagligvare.Dto.UserRequest;
import com.example.smartdagligvare.Model.AppUser;
import com.example.smartdagligvare.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users
    @GetMapping
    public List<AppUser> findAll() {
        return userService.findAll();
    }

    // GET /api/users/1
    @GetMapping("/{id}")
    public AppUser findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    // POST /api/users
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AppUser> save(
            @Valid @RequestBody UserRequest request
    ) {
        AppUser savedUser = userService.save(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);


    }

    // PUT /api/users/1
    @PutMapping("/{id}")
    public AppUser update(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request
    ) {

        return userService.update(id, request);
    }

    // DELETE /api/users/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable Long id
    ) {
        userService.deleteById(id);

        return ResponseEntity.noContent().build();
    }


}
