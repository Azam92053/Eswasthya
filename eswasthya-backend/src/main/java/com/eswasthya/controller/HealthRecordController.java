package com.eswasthya.controller;

import com.eswasthya.dto.request.HealthRecordRequest;
import com.eswasthya.dto.response.ApiResponse;
import com.eswasthya.dto.response.HealthRecordResponse;
import com.eswasthya.dto.response.HealthSummaryResponse;
import com.eswasthya.entity.User;
import com.eswasthya.service.HealthRecordService;
import com.eswasthya.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Authenticated endpoints for health records, dashboard summaries, and reports. */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthRecordController {

    private final HealthRecordService healthRecordService;
    private final UserService userService;

    @PostMapping("/records")
    public ResponseEntity<ApiResponse<HealthRecordResponse>> createRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody HealthRecordRequest request) {
        User user = userService.findByUsername(userDetails.getUsername());
        HealthRecordResponse created = healthRecordService.createRecord(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Health record saved successfully", created));
    }

    @GetMapping("/records")
    public ResponseEntity<ApiResponse<List<HealthRecordResponse>>> getAllRecords(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<HealthRecordResponse> records = healthRecordService.getAllRecordsForUser(user);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/records/{id}")
    public ResponseEntity<ApiResponse<HealthRecordResponse>> getRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User user = userService.findByUsername(userDetails.getUsername());
        HealthRecordResponse record = healthRecordService.getRecordById(id, user);
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    @GetMapping("/records/range")
    public ResponseEntity<ApiResponse<List<HealthRecordResponse>>> getRecordsByRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<HealthRecordResponse> records = healthRecordService.getRecordsInRange(user, from, to);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<ApiResponse<HealthRecordResponse>> updateRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody HealthRecordRequest request) {
        User user = userService.findByUsername(userDetails.getUsername());
        HealthRecordResponse updated = healthRecordService.updateRecord(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Health record updated", updated));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User user = userService.findByUsername(userDetails.getUsername());
        healthRecordService.deleteRecord(id, user);
        return ResponseEntity.ok(ApiResponse.success("Health record deleted"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<HealthSummaryResponse>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        HealthSummaryResponse summary = healthRecordService.getDashboardSummary(user);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/report")
    public ResponseEntity<byte[]> downloadReport(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        String report = healthRecordService.generateTextReport(user);

        String filename = "eswasthya-report-" + user.getUsername() + "-" + LocalDate.now() + ".txt";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(report.getBytes());
    }
}
