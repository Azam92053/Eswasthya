package com.eswasthya.service;

import com.eswasthya.dto.request.HealthRecordRequest;
import com.eswasthya.dto.response.HealthRecordResponse;
import com.eswasthya.dto.response.HealthSummaryResponse;
import com.eswasthya.entity.HealthRecord;
import com.eswasthya.entity.User;
import com.eswasthya.exception.BadRequestException;
import com.eswasthya.exception.ResourceNotFoundException;
import com.eswasthya.repository.AlertRepository;
import com.eswasthya.repository.HealthRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/** Business logic for health records, dashboards, alerts, and reports. */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;
    private final AlertRepository alertRepository;
    private final AlertService alertService;

    /** Saves a new health record and evaluates alert thresholds. */
    @Transactional
    public HealthRecordResponse createRecord(HealthRecordRequest request, User user) {
        // Prevent duplicate record on the same date
        if (healthRecordRepository.existsByUserAndRecordDate(user, request.getRecordDate())) {
            throw new BadRequestException(
                    "A health record for " + request.getRecordDate() + " already exists. " +
                    "Use the update endpoint to modify it.");
        }

        HealthRecord record = mapRequestToEntity(request, new HealthRecord());
        record.setUser(user);

        HealthRecord saved = healthRecordRepository.save(record);
        log.info("Health record created: id={} user={} date={}", saved.getId(),
                user.getUsername(), saved.getRecordDate());

        // Generate alerts in the same transaction.
        alertService.generateAlertsForRecord(saved, user);

        return HealthRecordResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<HealthRecordResponse> getAllRecordsForUser(User user) {
        return healthRecordRepository.findByUserOrderByRecordDateDesc(user)
                .stream()
                .map(HealthRecordResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HealthRecordResponse> getRecordsInRange(User user, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BadRequestException("'from' date must be before or equal to 'to' date");
        }
        return healthRecordRepository
                .findByUserAndRecordDateBetweenOrderByRecordDateDesc(user, from, to)
                .stream()
                .map(HealthRecordResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HealthRecordResponse getRecordById(Long id, User user) {
        return HealthRecordResponse.from(findOwnedRecord(id, user));
    }

    /** Updates a health record and regenerates its alerts. */
    @Transactional
    public HealthRecordResponse updateRecord(Long id, HealthRecordRequest request, User user) {
        HealthRecord record = findOwnedRecord(id, user);

        // If the date is changing, ensure no collision
        if (!record.getRecordDate().equals(request.getRecordDate())
                && healthRecordRepository.existsByUserAndRecordDate(user, request.getRecordDate())) {
            throw new BadRequestException(
                    "A health record for " + request.getRecordDate() + " already exists.");
        }

        // Delete old alerts tied to this record
        alertRepository.deleteAll(alertRepository.findByRecord(record));

        mapRequestToEntity(request, record);
        HealthRecord saved = healthRecordRepository.save(record);

        // Re-evaluate alerts with updated values
        alertService.generateAlertsForRecord(saved, user);

        log.info("Health record updated: id={} user={}", saved.getId(), user.getUsername());
        return HealthRecordResponse.from(saved);
    }

    @Transactional
    public void deleteRecord(Long id, User user) {
        HealthRecord record = findOwnedRecord(id, user);
        // Cascades will delete linked alerts via CascadeType.ALL on the entity
        healthRecordRepository.delete(record);
        log.info("Health record deleted: id={} user={}", id, user.getUsername());
    }

    /** Builds the user dashboard health summary. */
    @Transactional(readOnly = true)
    public HealthSummaryResponse getDashboardSummary(User user) {
        List<HealthRecord> allRecords = healthRecordRepository.findByUserOrderByRecordDateDesc(user);

        HealthRecord latest = allRecords.isEmpty() ? null : allRecords.get(0);

        // 30-day averages
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<HealthRecord> last30 = healthRecordRepository
                .findByUserAndRecordDateBetweenOrderByRecordDateDesc(user, thirtyDaysAgo, LocalDate.now());

        double avgBmi = average(last30.stream()
                .map(HealthRecord::getBmi).collect(Collectors.toList()));
        double avgGlucose = average(last30.stream()
                .map(HealthRecord::getGlucose).collect(Collectors.toList()));
        double avgSys = averageInt(last30.stream()
                .map(HealthRecord::getSystolicBp).collect(Collectors.toList()));

        long unreadAlerts = alertService.countUnreadAlerts(user);
        long totalAlerts = alertRepository.findByUserOrderByCreatedAtDesc(user).size();

        HealthSummaryResponse.HealthSummaryResponseBuilder builder = HealthSummaryResponse.builder()
                .userId(user.getId())
                .userName(user.getName())
                .totalRecords(allRecords.size())
                .avgBmi30Days(roundTwoDecimals(avgBmi))
                .avgGlucose30Days(roundTwoDecimals(avgGlucose))
                .avgSystolicBp30Days(roundTwoDecimals(avgSys))
                .unreadAlertCount(unreadAlerts)
                .totalAlertCount(totalAlerts)
                .recentRecords(allRecords.stream()
                        .limit(10)
                        .map(HealthRecordResponse::from)
                        .collect(Collectors.toList()));

        if (latest != null) {
            builder
                .latestBmi(latest.getBmi())
                .latestBmiCategory(bmiCategory(latest.getBmi()))
                .latestBloodPressure(latest.getBloodPressureString())
                .latestBpCategory(bpCategory(latest.getSystolicBp(), latest.getDiastolicBp()))
                .latestGlucose(latest.getGlucose())
                .latestGlucoseCategory(glucoseCategory(latest.getGlucose()))
                .latestActivityLevel(latest.getActivityLevel() != null
                        ? latest.getActivityLevel().name() : null);
        }

        return builder.build();
    }

    /** Generates a plain-text health summary report. */
    @Transactional(readOnly = true)
    public String generateTextReport(User user) {
        List<HealthRecord> records = healthRecordRepository.findByUserOrderByRecordDateDesc(user);
        HealthSummaryResponse summary = getDashboardSummary(user);

        StringBuilder sb = new StringBuilder();
        sb.append("=======================================================\n");
        sb.append("  eSwasthya — Health Summary Report\n");
        sb.append("=======================================================\n");
        sb.append("Name   : ").append(user.getName()).append("\n");
        sb.append("Role   : ").append(user.getRole()).append("\n");
        sb.append("Report : ").append(LocalDate.now()).append("\n");
        sb.append("-------------------------------------------------------\n\n");

        sb.append("LATEST SNAPSHOT\n");
        sb.append("  BMI          : ").append(fmt(summary.getLatestBmi()))
          .append("  (").append(nullSafe(summary.getLatestBmiCategory())).append(")\n");
        sb.append("  Blood Press  : ").append(nullSafe(summary.getLatestBloodPressure()))
          .append("  mmHg  (").append(nullSafe(summary.getLatestBpCategory())).append(")\n");
        sb.append("  Glucose      : ").append(fmt(summary.getLatestGlucose()))
          .append("  mg/dL  (").append(nullSafe(summary.getLatestGlucoseCategory())).append(")\n");
        sb.append("  Activity     : ").append(nullSafe(summary.getLatestActivityLevel())).append("\n\n");

        sb.append("30-DAY AVERAGES\n");
        sb.append("  Avg BMI        : ").append(fmt(summary.getAvgBmi30Days())).append("\n");
        sb.append("  Avg Glucose    : ").append(fmt(summary.getAvgGlucose30Days())).append(" mg/dL\n");
        sb.append("  Avg Systolic   : ").append(fmt(summary.getAvgSystolicBp30Days())).append(" mmHg\n\n");

        sb.append("ALERTS\n");
        sb.append("  Total    : ").append(summary.getTotalAlertCount()).append("\n");
        sb.append("  Unread   : ").append(summary.getUnreadAlertCount()).append("\n\n");

        sb.append("FULL RECORD HISTORY (").append(records.size()).append(" record(s))\n");
        sb.append("-------------------------------------------------------\n");
        for (HealthRecord r : records) {
            sb.append("[").append(r.getRecordDate()).append("]\n");
            if (r.getBmi() != null)        sb.append("  BMI     : ").append(r.getBmi()).append("\n");
            if (r.getBloodPressureString() != null) sb.append("  BP      : ").append(r.getBloodPressureString()).append(" mmHg\n");
            if (r.getGlucose() != null)    sb.append("  Glucose : ").append(r.getGlucose()).append(" mg/dL\n");
            if (r.getActivityLevel() != null) sb.append("  Activity: ").append(r.getActivityLevel()).append("\n");
            if (r.getNotes() != null)      sb.append("  Notes   : ").append(r.getNotes()).append("\n");
            sb.append("\n");
        }
        sb.append("=======================================================\n");
        sb.append("  End of Report — eSwasthya\n");
        sb.append("=======================================================\n");

        return sb.toString();
    }

    private HealthRecord findOwnedRecord(Long id, User user) {
        return healthRecordRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health record not found with id: " + id));
    }

    private HealthRecord mapRequestToEntity(HealthRecordRequest req, HealthRecord record) {
        record.setRecordDate(req.getRecordDate());
        record.setBmi(req.getBmi());
        record.setSystolicBp(req.getSystolicBp());
        record.setDiastolicBp(req.getDiastolicBp());
        record.setGlucose(req.getGlucose());
        record.setActivityLevel(req.getActivityLevel());
        record.setNotes(req.getNotes());
        return record;
    }

    private double average(List<Double> values) {
        return values.stream().filter(v -> v != null).mapToDouble(Double::doubleValue)
                .average().orElse(0);
    }

    private double averageInt(List<Integer> values) {
        return values.stream().filter(v -> v != null).mapToInt(Integer::intValue)
                .average().orElse(0);
    }

    private double roundTwoDecimals(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private String fmt(Double d) {
        return d == null ? "N/A" : String.format("%.2f", d);
    }

    private String nullSafe(String s) {
        return s == null ? "N/A" : s;
    }

    private String bmiCategory(Double bmi) {
        if (bmi == null) return null;
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    private String bpCategory(Integer sys, Integer dia) {
        if (sys == null || dia == null) return null;
        if (sys < 90 || dia < 60) return "Low";
        if (sys < 120 && dia < 80) return "Normal";
        if (sys < 130) return "Elevated";
        if (sys < 140) return "High Stage 1";
        return "High Stage 2";
    }

    private String glucoseCategory(Double gl) {
        if (gl == null) return null;
        if (gl < 70)  return "Low";
        if (gl < 100) return "Normal";
        if (gl < 126) return "Pre-diabetic";
        return "Diabetic";
    }
}
