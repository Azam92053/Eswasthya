package com.eswasthya.repository;

import com.eswasthya.entity.ApiCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ApiCacheRepository extends JpaRepository<ApiCache, Long> {

    Optional<ApiCache> findByEndpoint(String endpoint);

    /** Remove all cache entries older than the given cutoff time. */
    @Modifying
    @Query("DELETE FROM ApiCache c WHERE c.fetchedAt < :cutoff")
    void deleteExpiredEntries(LocalDateTime cutoff);
}
