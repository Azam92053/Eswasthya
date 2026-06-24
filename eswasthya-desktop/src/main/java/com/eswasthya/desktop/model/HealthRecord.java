package com.eswasthya.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthRecord {
    private Long    id;
    private Long    userId;
    private String  userName;
    private String  recordDate;
    private Double  bmi;
    private String  bmiCategory;
    private Integer systolicBp;
    private Integer diastolicBp;
    private String  bloodPressure;
    private String  bpCategory;
    private Double  glucose;
    private String  glucoseCategory;
    private String  activityLevel;
    private String  notes;
    private String  createdAt;

    // Getters
    public Long    getId()             { return id; }
    public Long    getUserId()         { return userId; }
    public String  getUserName()       { return userName; }
    public String  getRecordDate()     { return recordDate; }
    public Double  getBmi()            { return bmi; }
    public String  getBmiCategory()    { return bmiCategory; }
    public Integer getSystolicBp()     { return systolicBp; }
    public Integer getDiastolicBp()    { return diastolicBp; }
    public String  getBloodPressure()  { return bloodPressure; }
    public String  getBpCategory()     { return bpCategory; }
    public Double  getGlucose()        { return glucose; }
    public String  getGlucoseCategory(){ return glucoseCategory; }
    public String  getActivityLevel()  { return activityLevel; }
    public String  getNotes()          { return notes; }
    public String  getCreatedAt()      { return createdAt; }

    // Setters
    public void setId(Long v)              { this.id = v; }
    public void setUserId(Long v)          { this.userId = v; }
    public void setUserName(String v)      { this.userName = v; }
    public void setRecordDate(String v)    { this.recordDate = v; }
    public void setBmi(Double v)           { this.bmi = v; }
    public void setBmiCategory(String v)   { this.bmiCategory = v; }
    public void setSystolicBp(Integer v)   { this.systolicBp = v; }
    public void setDiastolicBp(Integer v)  { this.diastolicBp = v; }
    public void setBloodPressure(String v) { this.bloodPressure = v; }
    public void setBpCategory(String v)    { this.bpCategory = v; }
    public void setGlucose(Double v)       { this.glucose = v; }
    public void setGlucoseCategory(String v){ this.glucoseCategory = v; }
    public void setActivityLevel(String v) { this.activityLevel = v; }
    public void setNotes(String v)         { this.notes = v; }
    public void setCreatedAt(String v)     { this.createdAt = v; }

    public String getBmiFormatted() {
        return bmi != null ? String.format("%.1f", bmi) : "—";
    }
    public String getGlucoseFormatted() {
        return glucose != null ? String.format("%.0f mg/dL", glucose) : "—";
    }
    public String getBpFormatted() {
        return bloodPressure != null ? bloodPressure + " mmHg" : "—";
    }
}
