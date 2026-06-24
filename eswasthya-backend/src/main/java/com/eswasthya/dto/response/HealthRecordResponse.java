package com.eswasthya.dto.response;

import com.eswasthya.entity.HealthRecord;
import com.eswasthya.enums.ActivityLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class HealthRecordResponse {

    private Long id;
    private Long userId;
    private String userName;
    private LocalDate recordDate;

    // Metrics
    private Double bmi;
    private String bmiCategory;
    private Integer systolicBp;
    private Integer diastolicBp;
    private String bloodPressure;
    private String bpCategory;
    private Double glucose;
    private String glucoseCategory;
    private ActivityLevel activityLevel;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static HealthRecordResponse from(HealthRecord hr) {
        return HealthRecordResponse.builder()
                .id(hr.getId())
                .userId(hr.getUser().getId())
                .userName(hr.getUser().getName())
                .recordDate(hr.getRecordDate())
                .bmi(hr.getBmi())
                .bmiCategory(categorizeBmi(hr.getBmi()))
                .systolicBp(hr.getSystolicBp())
                .diastolicBp(hr.getDiastolicBp())
                .bloodPressure(hr.getBloodPressureString())
                .bpCategory(categorizeBp(hr.getSystolicBp(), hr.getDiastolicBp()))
                .glucose(hr.getGlucose())
                .glucoseCategory(categorizeGlucose(hr.getGlucose()))
                .activityLevel(hr.getActivityLevel())
                .notes(hr.getNotes())
                .createdAt(hr.getCreatedAt())
                .updatedAt(hr.getUpdatedAt())
                .build();
    }

    // Category helpers (WHO / AHA standards)

    private static String categorizeBmi(Double bmi) {
        if (bmi == null) return null;
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal weight";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    private static String categorizeBp(Integer sys, Integer dia) {
        if (sys == null || dia == null) return null;
        if (sys < 90 || dia < 60) return "Low (Hypotension)";
        if (sys < 120 && dia < 80) return "Normal";
        if (sys < 130 && dia < 80) return "Elevated";
        if (sys < 140 || dia < 90) return "High Stage 1";
        return "High Stage 2 (Hypertension)";
    }

    private static String categorizeGlucose(Double gl) {
        if (gl == null) return null;
        if (gl < 70) return "Low (Hypoglycaemia)";
        if (gl < 100) return "Normal (fasting)";
        if (gl < 126) return "Pre-diabetic";
        return "Diabetic range";
    }
}
