package com.retainiq.repository;

import com.retainiq.model.CampaignLog;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory repository for CampaignLog entries.
 * Uses ConcurrentHashMap instead of MongoDB — zero external DB dependency.
 */
@Repository
@Component
public class CampaignLogRepository {

    private final Map<String, CampaignLog> store = new ConcurrentHashMap<>();

    public CampaignLog save(CampaignLog log) {
        store.put(log.getId(), log);
        return log;
    }

    public Optional<CampaignLog> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<CampaignLog> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<CampaignLog> findByCustomerId(String customerId) {
        return store.values().stream()
                .filter(log -> customerId.equals(log.getCustomerId()))
                .collect(Collectors.toList());
    }

    public long count() {
        return store.size();
    }

    public long countByStatus(String status) {
        return store.values().stream()
                .filter(log -> status.equalsIgnoreCase(log.getStatus()))
                .count();
    }

    public void deleteAll() {
        store.clear();
    }
}
