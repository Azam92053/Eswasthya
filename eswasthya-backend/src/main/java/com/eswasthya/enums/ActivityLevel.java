package com.eswasthya.enums;

/**
 * Physical activity level self-reported by the user.
 * Used in health record entries and for generating activity-based alerts.
 */
public enum ActivityLevel {

    /** Little or no exercise — sedentary lifestyle */
    LOW,

    /** Light exercise 1–3 days/week */
    MODERATE,

    /** Hard exercise 6–7 days/week */
    HIGH
}
