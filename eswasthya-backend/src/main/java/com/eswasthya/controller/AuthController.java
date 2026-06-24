package com.eswasthya.controller;

import com.eswasthya.dto.request.LoginRequest;
import com.eswasthya.dto.request.RegisterRequest;
import com.eswasthya.dto.response.ApiResponse;
import com.eswasthya.dto.response.AuthResponse;
import com.eswasthya.dto.response.UserResponse;
import com.eswasthya.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints — publicly accessible (no JWT required).
 *
 * POST /api/auth/register — create a new account (FR-01)
 * POST /api/auth/login    — authenticate and receive JWT (FR-02)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse auth = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", auth));
    }
}
