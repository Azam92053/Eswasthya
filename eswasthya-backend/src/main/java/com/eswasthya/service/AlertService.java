package com.eswasthya.service;

import com.eswasthya.dto.response.AlertResponse;
import com.eswasthya.entity.Alert;
import com.eswasthya.entity.HealthRecord;
import com.eswasthya.entity.User;
import com.eswasthya.enums.AlertType;
import com.eswasthya.enums.ActivityLevel;
import com.eswasthya.exception.ResourceNotFoundException;
import com.eswasthya.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Generates, retrieves, and updates health alerts. */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;

    /** Creates alerts for out-of-range metrics in a saved health record. */
    @Transactional
    public List<Alert> generateAlertsForRecord(HealthRecord record, User user) {
        List<Alert> generatedAlerts = new ArrayList<>();

        generatedAlerts.addAll(checkBmi(record, user));
        generatedAlerts.addAll(checkBloodPressure(record, user));
        generatedAlerts.addAll(checkGlucose(record, user));
        generatedAlerts.addAll(checkActivityLevel(record, user));

        if (!generatedAlerts.isEmpty()) {
            List<Alert> saved = alertRepository.saveAll(generatedAlerts);
            log.info("Generated {} alert(s) for user {} on record {}",
                    saved.size(), user.getUsername(), record.getId());
            return saved;
        }
        return generatedAlerts;
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAlertsForUser(User user) {
        return alertRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(AlertResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getUnreadAlertsForUser(User user) {
        return alertRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(AlertResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countUnreadAlerts(User user) {
        return alertRepository.countByUserAndIsReadFalse(user);
    }

    // Mark as read

    /** Marks a single owned alert as read. */
    @Transactional
    public AlertResponse markAsRead(Long alertId, User user) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));

        if (!alert.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Alert", alertId);
        }

        alert.setIsRead(true);
        return AlertResponse.from(alertRepository.save(alert));
    }

    /** Marks all alerts for the user as read. */
    @Transactional
    public void markAllAsRead(User user) {
        alertRepository.markAllAsReadForUser(user);
        log.info("Marked all alerts read for user {}", user.getUsername());
    }

    // Threshold checks
    private List<Alert> checkBmi(HealthRecord record, User user) {
        List<Alert> alerts = new ArrayList<>();
        Double bmi = record.getBmi();
        if (bmi == null) return alerts;

        if (bmi < 18.5) {
            alerts.add(buildAlert(user, record, AlertType.BMI_UNDERWEIGHT,
                    String.format("Your BMI is %.1f — below the healthy range (18.5–24.9). " +
                            "Consider consulting a nutritionist.", bmi)));
        } else if (bmi >= 25.0 && bmi < 30.0) {
            alerts.add(buildAlert(user, record, AlertType.BMI_OVERWEIGHT,
                    String.format("Your BMI is %.1f — in the overweight range (25.0–29.9). " +
                            "Regular physical activity and a balanced diet are recommended.", bmi)));
        } else if (bmi >= 30.0) {
            alerts.add(buildAlert(user, record, AlertType.BMI_OBESE,
                    String.format("Your BMI is %.1f — in the obese range (≥30.0). " +
                            "Please consult a healthcare professional.", bmi)));
        }
        return alerts;
    }

    private List<Alert> checkBloodPressure(HealthRecord record, User user) {
        List<Alert> alerts = new ArrayList<>();
        Integer sys = record.getSystolicBp();
        Integer dia = record.getDiastolicBp();
        if (sys == null || dia == null) return alerts;

        if (sys < 90 || dia < 60) {
            alerts.add(buildAlert(user, record, AlertType.LOW_BP,
                    String.format("Blood pressure %d/%d mmHg is below normal (< 90/60). " +
                            "Monitor for dizziness or fatigue.", sys, dia)));
        } else if ((sys >= 130 && sys < 140) || (dia >= 80 && dia < 90)) {
            alerts.add(buildAlert(user, record, AlertType.HIGH_BP_STAGE1,
                    String.format("Blood pressure %d/%d mmHg indicates Stage 1 Hypertension. " +
                            "Lifestyle changes are advised.", sys, dia)));
        } else if (sys >= 140 || dia >= 90) {
            alerts.add(buildAlert(user, record, AlertType.HIGH_BP_STAGE2,
                    String.format("Blood pressure %d/%d mmHg indicates Stage 2 Hypertension. " +
                            "Seek medical attention promptly.", sys, dia)));
        }
        return alerts;
    }

    private List<Alert> checkGlucose(HealthRecord record, User user) {
        List<Alert> alerts = new ArrayList<>();
        Double gl = record.getGlucose();
        if (gl == null) return alerts;

        if (gl < 70) {
            alerts.add(buildAlert(user, record, AlertType.LOW_GLUCOSE,
                    String.format("Fasting glucose %.1f mg/dL is below normal (< 70). " +
                            "Consume a fast-acting carbohydrate and seek advice.", gl)));
        } else if (gl >= 100 && gl < 126) {
            alerts.add(buildAlert(user, record, AlertType.GLUCOSE_PREDIABETIC,
                    String.format("Fasting glucose %.1f mg/dL is in the pre-diabetic range " +
                            "(100–125 mg/dL). Diet and exercise changes are strongly recommended.", gl)));
        } else if (gl >= 126) {
            alerts.add(buildAlert(user, record, AlertType.GLUCOSE_DIABETIC,
                    String.format("Fasting glucose %.1f mg/dL is in the diabetic range (≥126 mg/dL). " +
                            "Please consult your doctor immediately.", gl)));
        }
        return alerts;
    }

    private List<Alert> checkActivityLevel(HealthRecord record, User user) {
        List<Alert> alerts = new ArrayList<>();
        if (ActivityLevel.LOW.equals(record.getActivityLevel())) {
            alerts.add(buildAlert(user, record, AlertType.SEDENTARY_LIFESTYLE,
                    "Your activity level is LOW. A sedentary lifestyle increases the risk of " +
                    "cardiovascular disease and diabetes. Aim for at least 150 minutes of " +
                    "moderate activity per week."));
        }
        return alerts;
    }

    // Builder helper

    private Alert buildAlert(User user, HealthRecord record, AlertType type, String message) {
        return Alert.builder()
                .user(user)
                .record(record)
                .alertType(type)
                .message(message)
                .isRead(false)
                .build();
    }
}
