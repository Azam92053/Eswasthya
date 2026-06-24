package com.eswasthya.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardSummary {
    private Long   userId;
    private String userName;
    private Double latestBmi;
    private String latestBmiCategory;
    private String latestBloodPressure;
    private String latestBpCategory;
    private Double latestGlucose;
    private String latestGlucoseCategory;
    private String latestActivityLevel;
    private Double avgBmi30Days;
    private Double avgGlucose30Days;
    private Double avgSystolicBp30Days;
    private long   unreadAlertCount;
    private long   totalAlertCount;
    private int    totalRecords;
    private List<HealthRecord> recentRecords;

    public Long   getUserId()               { return userId; }
    public String getUserName()             { return userName; }
    public Double getLatestBmi()            { return latestBmi; }
    public String getLatestBmiCategory()    { return latestBmiCategory; }
    public String getLatestBloodPressure()  { return latestBloodPressure; }
    public String getLatestBpCategory()     { return latestBpCategory; }
    public Double getLatestGlucose()        { return latestGlucose; }
    public String getLatestGlucoseCategory(){ return latestGlucoseCategory; }
    public String getLatestActivityLevel()  { return latestActivityLevel; }
    public Double getAvgBmi30Days()         { return avgBmi30Days; }
    public Double getAvgGlucose30Days()     { return avgGlucose30Days; }
    public Double getAvgSystolicBp30Days()  { return avgSystolicBp30Days; }
    public long   getUnreadAlertCount()     { return unreadAlertCount; }
    public long   getTotalAlertCount()      { return totalAlertCount; }
    public int    getTotalRecords()         { return totalRecords; }
    public List<HealthRecord> getRecentRecords() { return recentRecords; }

    public void setUserId(Long v)                { this.userId = v; }
    public void setUserName(String v)            { this.userName = v; }
    public void setLatestBmi(Double v)           { this.latestBmi = v; }
    public void setLatestBmiCategory(String v)   { this.latestBmiCategory = v; }
    public void setLatestBloodPressure(String v) { this.latestBloodPressure = v; }
    public void setLatestBpCategory(String v)    { this.latestBpCategory = v; }
    public void setLatestGlucose(Double v)       { this.latestGlucose = v; }
    public void setLatestGlucoseCategory(String v){ this.latestGlucoseCategory = v; }
    public void setLatestActivityLevel(String v) { this.latestActivityLevel = v; }
    public void setAvgBmi30Days(Double v)        { this.avgBmi30Days = v; }
    public void setAvgGlucose30Days(Double v)    { this.avgGlucose30Days = v; }
    public void setAvgSystolicBp30Days(Double v) { this.avgSystolicBp30Days = v; }
    public void setUnreadAlertCount(long v)      { this.unreadAlertCount = v; }
    public void setTotalAlertCount(long v)       { this.totalAlertCount = v; }
    public void setTotalRecords(int v)           { this.totalRecords = v; }
    public void setRecentRecords(List<HealthRecord> v){ this.recentRecords = v; }
}
