package com.eswasthya.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Alert {
    private Long    id;
    private Long    userId;
    private Long    recordId;
    private String  alertType;
    private String  message;
    private Boolean isRead;
    private String  createdAt;

    public Long    getId()         { return id; }
    public Long    getUserId()     { return userId; }
    public Long    getRecordId()   { return recordId; }
    public String  getAlertType()  { return alertType; }
    public String  getMessage()    { return message; }
    public Boolean getIsRead()     { return isRead; }
    public String  getCreatedAt()  { return createdAt; }

    public void setId(Long v)          { this.id = v; }
    public void setUserId(Long v)      { this.userId = v; }
    public void setRecordId(Long v)    { this.recordId = v; }
    public void setAlertType(String v) { this.alertType = v; }
    public void setMessage(String v)   { this.message = v; }
    public void setIsRead(Boolean v)   { this.isRead = v; }
    public void setCreatedAt(String v) { this.createdAt = v; }

    public String getAlertIcon() {
        if (alertType == null) return "⚠";
        return switch (alertType) {
            case "BMI_UNDERWEIGHT","BMI_OVERWEIGHT","BMI_OBESE"         -> "⚖";
            case "HIGH_BP_STAGE1","HIGH_BP_STAGE2","LOW_BP"             -> "♥";
            case "LOW_GLUCOSE","GLUCOSE_PREDIABETIC","GLUCOSE_DIABETIC" -> "🩸";
            case "SEDENTARY_LIFESTYLE"                                  -> "🏃";
            default                                                     -> "⚠";
        };
    }

    public String getAlertLabel() {
        if (alertType == null) return "Alert";
        return switch (alertType) {
            case "BMI_UNDERWEIGHT"     -> "BMI – Underweight";
            case "BMI_OVERWEIGHT"      -> "BMI – Overweight";
            case "BMI_OBESE"           -> "BMI – Obese";
            case "HIGH_BP_STAGE1"      -> "BP – High Stage 1";
            case "HIGH_BP_STAGE2"      -> "BP – High Stage 2";
            case "LOW_BP"              -> "BP – Low";
            case "LOW_GLUCOSE"         -> "Glucose – Low";
            case "GLUCOSE_PREDIABETIC" -> "Glucose – Pre-diabetic";
            case "GLUCOSE_DIABETIC"    -> "Glucose – Diabetic";
            case "SEDENTARY_LIFESTYLE" -> "Sedentary Lifestyle";
            default                    -> alertType;
        };
    }
}
