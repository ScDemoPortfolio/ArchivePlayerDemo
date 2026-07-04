package com.archiveplayer.controllers;

import com.archiveplayer.dto.AuthRequestDTO;
import com.archiveplayer.dto.AuthResponseDTO;
import com.archiveplayer.entities.Account;
import com.archiveplayer.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AuthRequestDTO registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("Account successfully created!");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(@Valid @RequestBody AuthRequestDTO loginRequest) {
        return ResponseEntity.ok(authService.loginUser(loginRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@AuthenticationPrincipal Account account) {
        authService.logoutUser(account);
        return ResponseEntity.ok("Logged out successfully.");
    }

    @GetMapping("/validate-session")
    public ResponseEntity<?> validateSession(@AuthenticationPrincipal Account account) {
        if (account != null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired or active elsewhere.");
    }
}