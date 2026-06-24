package com.eswasthya.desktop.api;

import com.eswasthya.desktop.AuthSession;
import com.eswasthya.desktop.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

/** Typed facade for backend API endpoints. */
public class ApiService {

    private static ApiService instance;
    private final ApiClient   client = ApiClient.getInstance();

    private ApiService() {}

    public static ApiService getInstance() {
        if (instance == null) instance = new ApiService();
        return instance;
    }

    // Auth

    public void login(String username, String password) throws ApiException {
        Map<String, String> body = Map.of("username", username, "password", password);
        JsonNode data = client.post("/auth/login", body, new TypeReference<JsonNode>() {});
        String token = data.get("token").asText();
        UserInfo user = client.getMapper().convertValue(data, UserInfo.class);
        user.setUsername(data.get("username").asText());
        user.setName(data.get("name").asText());
        user.setRole(data.get("role").asText());
        // Preserve the backend user id when it is returned.
        if (data.has("userId")) user.setId(data.get("userId").asLong());
        AuthSession.getInstance().login(token, user);
    }

    public UserInfo register(Map<String, Object> payload) throws ApiException {
        return client.post("/auth/register", payload, new TypeReference<UserInfo>() {});
    }

    // Users

    public UserInfo getProfile() throws ApiException {
        return client.get("/users/profile", new TypeReference<UserInfo>() {});
    }

    public UserInfo updateProfile(Map<String, Object> payload) throws ApiException {
        return client.patch("/users/profile", payload, new TypeReference<UserInfo>() {});
    }

    // Health records

    public List<HealthRecord> getAllRecords() throws ApiException {
        return client.get("/health/records", new TypeReference<List<HealthRecord>>() {});
    }

    public HealthRecord createRecord(Map<String, Object> payload) throws ApiException {
        return client.post("/health/records", payload, new TypeReference<HealthRecord>() {});
    }

    public HealthRecord updateRecord(Long id, Map<String, Object> payload) throws ApiException {
        return client.put("/health/records/" + id, payload, new TypeReference<HealthRecord>() {});
    }

    public void deleteRecord(Long id) throws ApiException {
        client.delete("/health/records/" + id);
    }

    public DashboardSummary getDashboard() throws ApiException {
        return client.get("/health/dashboard", new TypeReference<DashboardSummary>() {});
    }

    public byte[] downloadReport() throws ApiException {
        return client.getBytes("/health/report");
    }

    // Alerts

    public List<Alert> getAllAlerts() throws ApiException {
        return client.get("/alerts", new TypeReference<List<Alert>>() {});
    }

    public long getUnreadAlertCount() throws ApiException {
        return client.get("/alerts/count", new TypeReference<Long>() {});
    }

    public Alert markAlertRead(Long id) throws ApiException {
        return client.put("/alerts/" + id + "/read", Map.of(),
                new TypeReference<Alert>() {});
    }

    public void markAllAlertsRead() throws ApiException {
        client.put("/alerts/read-all", Map.of(), new TypeReference<JsonNode>() {});
    }

    // Admin

    public AdminStats getAdminStats() throws ApiException {
        return client.get("/admin/stats", new TypeReference<AdminStats>() {});
    }

    public List<UserInfo> getAllUsers() throws ApiException {
        return client.get("/admin/users", new TypeReference<List<UserInfo>>() {});
    }

    public List<HealthRecord> getAllHealthRecords() throws ApiException {
        return client.get("/admin/health-records", new TypeReference<List<HealthRecord>>() {});
    }
}
