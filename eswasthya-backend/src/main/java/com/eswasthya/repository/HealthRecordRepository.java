package com.eswasthya.repository;

import com.eswasthya.entity.HealthRecord;
import com.eswasthya.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {

    /** All records for a user, sorted newest first. */
    List<HealthRecord> findByUserOrderByRecordDateDesc(User user);

    /** Records for a user within a date range. */
    List<HealthRecord> findByUserAndRecordDateBetweenOrderByRecordDateDesc(
            User user, LocalDate from, LocalDate to);

    /** Find a specific record belonging to a user. */
    Optional<HealthRecord> findByIdAndUser(Long id, User user);

    /** Latest single record for a user. */
    Optional<HealthRecord> findTopByUserOrderByRecordDateDesc(User user);

    // Admin aggregate queries

    /** Average BMI across all records. */
    @Query("SELECT AVG(hr.bmi) FROM HealthRecord hr WHERE hr.bmi IS NOT NULL")
    Double findAverageBmi();

    /** Average glucose across all records. */
    @Query("SELECT AVG(hr.glucose) FROM HealthRecord hr WHERE hr.glucose IS NOT NULL")
    Double findAverageGlucose();

    /** Average systolic BP across all records. */
    @Query("SELECT AVG(hr.systolicBp) FROM HealthRecord hr WHERE hr.systolicBp IS NOT NULL")
    Double findAverageSystolicBp();

    /** Count of records per activity level. */
    @Query("SELECT hr.activityLevel, COUNT(hr) FROM HealthRecord hr GROUP BY hr.activityLevel")
    List<Object[]> countByActivityLevel();

    /** Count of records for a specific user. */
    long countByUser(User user);

    /** Check if a record on a given date already exists for the user. */
    boolean existsByUserAndRecordDate(User user, LocalDate recordDate);

    /** All records for admin overview, ordered by date desc. */
    @Query("SELECT hr FROM HealthRecord hr JOIN FETCH hr.user ORDER BY hr.recordDate DESC")
    List<HealthRecord> findAllWithUsers();

    /** Per-user average BMI for admin dashboard. */
    @Query("SELECT u.name, AVG(hr.bmi) FROM HealthRecord hr JOIN hr.user u GROUP BY u.id, u.name")
    List<Object[]> averageBmiPerUser();
}
