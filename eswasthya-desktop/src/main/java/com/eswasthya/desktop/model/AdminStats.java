package com.eswasthya.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminStats {
    private long   totalUsers;
    private long   totalHealthRecords;
    private long   totalAlerts;
    private long   unreadAlerts;
    private Double averageBmi;
    private Double averageGlucose;
    private Double averageSystolicBp;
    private Map<String, Long> usersByRole;
    private Map<String, Long> alertsByType;
    private Map<String, Long> recordsByActivityLevel;
    private List<UserSummary> userSummaries;

    public long   getTotalUsers()           { return totalUsers; }
    public long   getTotalHealthRecords()   { return totalHealthRecords; }
    public long   getTotalAlerts()          { return totalAlerts; }
    public long   getUnreadAlerts()         { return unreadAlerts; }
    public Double getAverageBmi()           { return averageBmi; }
    public Double getAverageGlucose()       { return averageGlucose; }
    public Double getAverageSystolicBp()    { return averageSystolicBp; }
    public Map<String,Long> getUsersByRole()        { return usersByRole; }
    public Map<String,Long> getAlertsByType()       { return alertsByType; }
    public Map<String,Long> getRecordsByActivityLevel() { return recordsByActivityLevel; }
    public List<UserSummary> getUserSummaries()     { return userSummaries; }

    public void setTotalUsers(long v)           { this.totalUsers = v; }
    public void setTotalHealthRecords(long v)   { this.totalHealthRecords = v; }
    public void setTotalAlerts(long v)          { this.totalAlerts = v; }
    public void setUnreadAlerts(long v)         { this.unreadAlerts = v; }
    public void setAverageBmi(Double v)         { this.averageBmi = v; }
    public void setAverageGlucose(Double v)     { this.averageGlucose = v; }
    public void setAverageSystolicBp(Double v)  { this.averageSystolicBp = v; }
    public void setUsersByRole(Map<String,Long> v)   { this.usersByRole = v; }
    public void setAlertsByType(Map<String,Long> v)  { this.alertsByType = v; }
    public void setRecordsByActivityLevel(Map<String,Long> v){ this.recordsByActivityLevel = v; }
    public void setUserSummaries(List<UserSummary> v){ this.userSummaries = v; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserSummary {
        private Long   userId;
        private String name;
        private String role;
        private long   recordCount;
        private Double latestBmi;
        private String latestBmiCategory;
        private long   unreadAlertCount;

        public Long   getUserId()           { return userId; }
        public String getName()             { return name; }
        public String getRole()             { return role; }
        public long   getRecordCount()      { return recordCount; }
        public Double getLatestBmi()        { return latestBmi; }
        public String getLatestBmiCategory(){ return latestBmiCategory; }
        public long   getUnreadAlertCount() { return unreadAlertCount; }

        public void setUserId(Long v)            { this.userId = v; }
        public void setName(String v)            { this.name = v; }
        public void setRole(String v)            { this.role = v; }
        public void setRecordCount(long v)       { this.recordCount = v; }
        public void setLatestBmi(Double v)       { this.latestBmi = v; }
        public void setLatestBmiCategory(String v){ this.latestBmiCategory = v; }
        public void setUnreadAlertCount(long v)  { this.unreadAlertCount = v; }
    }
}
