package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.entity.AuditEntityType;
import com.lyubenblagoev.postfixrest.entity.AuditScope;
import com.lyubenblagoev.postfixrest.service.model.AuditLogResource;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AuditLogService {

    String REDACTED_VALUE = "[REDACTED]";

    void recordCreated(AuditEntityType entityType, Long entityId, Map<String, ?> newValues);

    void recordUpdated(AuditEntityType entityType, Long entityId, Map<String, ?> oldValues,
                       Map<String, ?> newValues);

    void recordUpdated(AuditEntityType entityType, Long entityId, Map<String, ?> oldValues,
                       Map<String, ?> newValues, Set<String> changedFields);

    void recordDeleted(AuditEntityType entityType, Long entityId, Map<String, ?> oldValues);

    List<AuditLogResource> find(AuditEntityType entityType, Long entityId, int limit);

    List<AuditLogResource> find(AuditScope scope, Long scopeId, int limit);
}
