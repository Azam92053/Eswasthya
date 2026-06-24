package com.eswasthya.dto.request;

import com.eswasthya.enums.ActivityLevel;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Payload for creating or updating a health record.
 *
 * <p>All metric fields are optional — a record can be a partial entry
 * (e.g., only BMI logged today). Input validation enforces medically
 * realistic bounds before the record reaches the service layer.</p>
 */
@Data
public class HealthRecordRequest {

    @NotNull(message = "Record date is required")
    @PastOrPresent(message = "Record date cannot be in the future")
    private LocalDate recordDate;

    // BMI
    @DecimalMin(value = "5.0", message = "BMI must be at least 5.0 (extreme minimum)")
    @DecimalMax(value = "100.0", message = "BMI must be at most 100.0")
    private Double bmi;

    // Blood Pressure
    @Min(value = 50, message = "Systolic BP must be at least 50 mmHg")
    @Max(value = 300, message = "Systolic BP must be at most 300 mmHg")
    private Integer systolicBp;

    @Min(value = 30, message = "Diastolic BP must be at least 30 mmHg")
    @Max(value = 200, message = "Diastolic BP must be at most 200 mmHg")
    private Integer diastolicBp;

    // Blood Glucose (fasting, mg/dL)
    @DecimalMin(value = "20.0", message = "Glucose must be at least 20 mg/dL")
    @DecimalMax(value = "800.0", message = "Glucose must be at most 800 mg/dL")
    private Double glucose;

    // Activity Level
    private ActivityLevel activityLevel;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}
