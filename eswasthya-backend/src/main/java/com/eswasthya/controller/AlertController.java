package com.eswasthya.controller;

import com.eswasthya.dto.response.AlertResponse;
import com.eswasthya.dto.response.ApiResponse;
import com.eswasthya.entity.User;
import com.eswasthya.service.AlertService;
import com.eswasthya.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Alert management for authenticated users.
 *
 * GET  /api/alerts           — all alerts (FR-06)
 * GET  /api/alerts/unread    — unread alerts only
 * GET  /api/alerts/count     — count of unread
 * PUT  /api/alerts/{id}/read — mark single alert read
 * PUT  /api/alerts/read-all  — mark all alerts read
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAllAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(alertService.getAlertsForUser(user)));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getUnreadAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(alertService.getUnreadAlertsForUser(user)));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        long count = alertService.countUnreadAlerts(user);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<AlertResponse>> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User user = userService.findByUsername(userDetails.getUsername());
        AlertResponse updated = alertService.markAsRead(id, user);
        return ResponseEntity.ok(ApiResponse.success("Alert marked as read", updated));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        alertService.markAllAsRead(user);
        return ResponseEntity.ok(ApiResponse.success("All alerts marked as read"));
    }
}
