package com.eswasthya.controller;

import com.eswasthya.dto.response.AdminStatsResponse;
import com.eswasthya.dto.response.ApiResponse;
import com.eswasthya.dto.response.HealthRecordResponse;
import com.eswasthya.dto.response.UserResponse;
import com.eswasthya.service.AdminService;
import com.eswasthya.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only endpoints — requires ROLE_ADMIN (enforced by SecurityConfig + @PreAuthorize).
 *
 * GET /api/admin/stats          — platform-wide health statistics (FR-08)
 * GET /api/admin/users          — list all users
 * GET /api/admin/users/{id}     — get specific user
 * GET /api/admin/health-records — all health records across all users
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getPlatformStats() {
        AdminStatsResponse stats = adminService.getPlatformStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/health-records")
    public ResponseEntity<ApiResponse<List<HealthRecordResponse>>> getAllHealthRecords() {
        List<HealthRecordResponse> records = adminService.getAllHealthRecords();
        return ResponseEntity.ok(ApiResponse.success(records));
    }
}
