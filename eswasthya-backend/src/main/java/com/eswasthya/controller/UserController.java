package com.eswasthya.controller;

import com.eswasthya.dto.request.UpdateUserRequest;
import com.eswasthya.dto.response.ApiResponse;
import com.eswasthya.dto.response.UserResponse;
import com.eswasthya.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * User profile management.
 *
 * GET   /api/users/profile  — view own profile
 * PATCH /api/users/profile  — update own profile
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse profile = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse updated = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }
}
