package com.eswasthya.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/** Aggregated statistics returned to the admin dashboard. */
@Data
@Builder
public class AdminStatsResponse {

    // User Stats
    private long totalUsers;
    private Map<String, Long> usersByRole;

    // Record Stats
    private long totalHealthRecords;
    private Double averageBmi;
    private Double averageGlucose;
    private Double averageSystolicBp;
    private Map<String, Long> recordsByActivityLevel;

    // Alert Stats
    private long totalAlerts;
    private long unreadAlerts;
    private Map<String, Long> alertsByType;

    // Per-User Summary
    /** List of all users with their latest health record summary. */
    private java.util.List<UserHealthSummary> userSummaries;

    @Data
    @Builder
    public static class UserHealthSummary {
        private Long userId;
        private String name;
        private String role;
        private long recordCount;
        private Double latestBmi;
        private String latestBmiCategory;
        private long unreadAlertCount;
    }
}
