package com.eswasthya.controller;

import com.eswasthya.dto.response.ApiResponse;
import com.eswasthya.service.ExternalHealthApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proxy to external health-reference API with DB-backed caching.
 * These endpoints are public and do not require JWT authentication.
 */
@RestController
@RequestMapping("/api/health/external")
@RequiredArgsConstructor
public class ExternalHealthApiController {

    private final ExternalHealthApiService externalHealthApiService;

    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<String>> getHealthTopics() {
        String data = externalHealthApiService.getHealthTopics();
        return ResponseEntity.ok(ApiResponse.success("Health topics fetched successfully", data));
    }

    @GetMapping("/bmi")
    public ResponseEntity<ApiResponse<String>> getBmiInfo() {
        String data = externalHealthApiService.getBmiInfo();
        return ResponseEntity.ok(ApiResponse.success("BMI information fetched successfully", data));
    }

    @GetMapping("/nutrition")
    public ResponseEntity<ApiResponse<String>> getNutritionInfo() {
        String data = externalHealthApiService.getNutritionInfo();
        return ResponseEntity.ok(ApiResponse.success("Nutrition information fetched successfully", data));
    }

    @DeleteMapping("/cache")
    public ResponseEntity<ApiResponse<Void>> evictCache() {
        externalHealthApiService.evictExpiredCache();
        return ResponseEntity.ok(ApiResponse.success("Cache eviction triggered"));
    }
}
