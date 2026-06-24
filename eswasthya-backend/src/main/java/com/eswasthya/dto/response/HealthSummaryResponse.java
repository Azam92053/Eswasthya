package com.eswasthya.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Health trend summary for the authenticated user's dashboard. */
@Data
@Builder
public class HealthSummaryResponse {

    private Long userId;
    private String userName;

    // Latest metrics snapshot
    private Double latestBmi;
    private String latestBmiCategory;
    private String latestBloodPressure;
    private String latestBpCategory;
    private Double latestGlucose;
    private String latestGlucoseCategory;
    private String latestActivityLevel;

    // Trend averages (last 30 days)
    private Double avgBmi30Days;
    private Double avgGlucose30Days;
    private Double avgSystolicBp30Days;

    // Alert summary
    private long unreadAlertCount;
    private long totalAlertCount;

    // Record history (full list, newest first)
    private int totalRecords;
    private List<HealthRecordResponse> recentRecords;
}
