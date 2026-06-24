package com.eswasthya.service;

import com.eswasthya.entity.ApiCache;
import com.eswasthya.repository.ApiCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

/** Fetches external health reference data and falls back to cached responses. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalHealthApiService {

    private static final String TOPICS_ENDPOINT_KEY  = "health_topics";
    private static final String BMI_ENDPOINT_KEY      = "bmi_info";
    private static final String NUTRITION_ENDPOINT_KEY = "nutrition_info";

    private final RestTemplate restTemplate;
    private final ApiCacheRepository apiCacheRepository;

    @Value("${app.external.health-api.base-url}")
    private String baseUrl;

    @Value("${app.external.health-api.cache-ttl-hours}")
    private int cacheTtlHours;

    @Value("${app.external.health-api.enabled}")
    private boolean apiEnabled;

    /** Fetches general health topic recommendations. */
    @Transactional
    public String getHealthTopics() {
        return fetchWithCache(
                TOPICS_ENDPOINT_KEY,
                baseUrl + "/topicsearch.json?lang=en&limit=10"
        );
    }

    /** Fetches BMI reference information and healthy weight guidelines. */
    @Transactional
    public String getBmiInfo() {
        return fetchWithCache(
                BMI_ENDPOINT_KEY,
                baseUrl + "/topicsearch.json?lang=en&keyword=BMI"
        );
    }

    /** Fetches dietary and nutrition reference information. */
    @Transactional
    public String getNutritionInfo() {
        return fetchWithCache(
                NUTRITION_ENDPOINT_KEY,
                baseUrl + "/topicsearch.json?lang=en&keyword=nutrition"
        );
    }

    /** Evicts expired cache entries. */
    @Transactional
    @Async
    public void evictExpiredCache() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(cacheTtlHours);
        apiCacheRepository.deleteExpiredEntries(cutoff);
        log.info("Evicted expired API cache entries older than {} hours", cacheTtlHours);
    }

    private String fetchWithCache(String cacheKey, String url) {
        Optional<ApiCache> cached = apiCacheRepository.findByEndpoint(cacheKey);
        if (cached.isPresent() && !cached.get().isExpired(cacheTtlHours)) {
            log.debug("Cache HIT for key: {}", cacheKey);
            return cached.get().getResponseData();
        }

        if (apiEnabled) {
            try {
                String response = restTemplate.getForObject(url, String.class);
                if (response != null) {
                    upsertCache(cacheKey, response);
                    log.info("Fetched and cached external API response for key: {}", cacheKey);
                    return response;
                }
            } catch (RestClientException e) {
                log.warn("External API call failed for key '{}': {}. Falling back to cache.",
                        cacheKey, e.getMessage());
            }
        }

        if (cached.isPresent()) {
            log.warn("Returning stale cache for key: {}", cacheKey);
            return cached.get().getResponseData();
        }

        return buildOfflineResponse(cacheKey);
    }

    private void upsertCache(String key, String responseData) {
        Optional<ApiCache> existing = apiCacheRepository.findByEndpoint(key);
        if (existing.isPresent()) {
            ApiCache entry = existing.get();
            entry.setResponseData(responseData);
            // Reinsert to refresh the timestamp.
            apiCacheRepository.delete(entry);
        }
        apiCacheRepository.save(ApiCache.builder()
                .endpoint(key)
                .responseData(responseData)
                .build());
    }

    private String buildOfflineResponse(String key) {
        return String.format(
                "{\"status\":\"offline\",\"message\":\"Health reference data for '%s' is " +
                "temporarily unavailable. Please try again later.\",\"cached\":false}", key);
    }
}
