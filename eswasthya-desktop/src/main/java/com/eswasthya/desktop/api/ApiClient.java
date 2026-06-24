package com.eswasthya.desktop.api;

import com.eswasthya.desktop.AuthSession;
import com.eswasthya.desktop.EswasthyaDesktopApp;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** HTTP client wrapper for authenticated backend requests. */
public class ApiClient {

    private static ApiClient instance;

    private final HttpClient   httpClient;
    private final ObjectMapper mapper;
    private final String       baseUrl;

    private ApiClient() {
        this.baseUrl = EswasthyaDesktopApp.BASE_URL + "/api";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static ApiClient getInstance() {
        if (instance == null) instance = new ApiClient();
        return instance;
    }

    // Core HTTP methods

    public <T> T get(String path, TypeReference<T> type) throws ApiException {
        HttpRequest req = builder(path).GET().build();
        return send(req, type);
    }

    public <T> T post(String path, Object body, TypeReference<T> type) throws ApiException {
        HttpRequest req = builder(path)
                .POST(jsonBody(body))
                .header("Content-Type", "application/json")
                .build();
        return send(req, type);
    }

    public <T> T put(String path, Object body, TypeReference<T> type) throws ApiException {
        HttpRequest req = builder(path)
                .PUT(jsonBody(body))
                .header("Content-Type", "application/json")
                .build();
        return send(req, type);
    }

    public <T> T patch(String path, Object body, TypeReference<T> type) throws ApiException {
        HttpRequest req = builder(path)
                .method("PATCH", jsonBody(body))
                .header("Content-Type", "application/json")
                .build();
        return send(req, type);
    }

    public void delete(String path) throws ApiException {
        HttpRequest req = builder(path).DELETE().build();
        sendRaw(req);
    }

    /** Download raw bytes (for report download). */
    public byte[] getBytes(String path) throws ApiException {
        try {
            HttpRequest req = builder(path).GET().build();
            HttpResponse<byte[]> res = httpClient.send(req,
                    HttpResponse.BodyHandlers.ofByteArray());
            if (res.statusCode() >= 400) throw new ApiException("Download failed: " + res.statusCode());
            return res.body();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Network error: " + e.getMessage());
        }
    }

    // Internal helpers

    private HttpRequest.Builder builder(String path) {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json");
        String token = AuthSession.getInstance().getToken();
        if (token != null) b.header("Authorization", "Bearer " + token);
        return b;
    }

    private HttpRequest.BodyPublisher jsonBody(Object body) {
        try {
            return HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body));
        } catch (Exception e) {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    private <T> T send(HttpRequest req, TypeReference<T> type) throws ApiException {
        try {
            HttpResponse<String> res = httpClient.send(req,
                    HttpResponse.BodyHandlers.ofString());
            checkStatus(res);
            JsonNode root = mapper.readTree(res.body());
            if (!root.path("success").asBoolean()) {
                throw new ApiException(extractErrorMessage(root, "Request failed"));
            }
            // Empty responses are valid for ApiResponse<Void>.
            JsonNode dataNode = root.get("data");
            if (dataNode == null || dataNode.isNull()) return null;
            return mapper.convertValue(dataNode, type);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Network error: " + e.getMessage());
        }
    }

    private void sendRaw(HttpRequest req) throws ApiException {
        try {
            HttpResponse<String> res = httpClient.send(req,
                    HttpResponse.BodyHandlers.ofString());
            checkStatus(res);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Network error: " + e.getMessage());
        }
    }

    private void checkStatus(HttpResponse<String> res) throws ApiException {
        int code = res.statusCode();
        if (code == 401) throw new ApiException("Session expired. Please log in again.");
        if (code == 403) throw new ApiException("You do not have permission to perform this action.");
        if (code == 404) throw new ApiException("Resource not found.");
        if (code >= 500) throw new ApiException("Server error. Please try again later.");
        if (code >= 400) {
            try {
                JsonNode node = mapper.readTree(res.body());
                throw new ApiException(extractErrorMessage(node, "Bad request"));
            } catch (ApiException e2) { throw e2; }
            catch (Exception ignored) {}
            throw new ApiException("Request failed with status " + code);
        }
    }

    private String extractErrorMessage(JsonNode node, String fallback) {
        String message = node.path("message").asText(fallback);
        JsonNode errors = node.path("errors");
        if (errors.isObject() && errors.size() > 0) {
            StringBuilder details = new StringBuilder(message);
            errors.fields().forEachRemaining(entry -> {
                if (details.length() > message.length()) details.append("; ");
                else details.append(": ");
                details.append(entry.getKey()).append(" ");
                JsonNode value = entry.getValue();
                details.append(value.isArray() && value.size() > 0 ? value.get(0).asText() : value.asText());
            });
            return details.toString();
        }
        if (errors.isArray() && errors.size() > 0) {
            return message + ": " + errors.get(0).asText();
        }
        return message;
    }

    public ObjectMapper getMapper() { return mapper; }
}
