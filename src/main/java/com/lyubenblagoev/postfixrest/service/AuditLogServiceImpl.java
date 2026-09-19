package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.entity.AuditAction;
import com.lyubenblagoev.postfixrest.entity.AuditEntityType;
import com.lyubenblagoev.postfixrest.entity.AuditLog;
import com.lyubenblagoev.postfixrest.entity.AuditLogChange;
import com.lyubenblagoev.postfixrest.repository.AuditLogRepository;
import com.lyubenblagoev.postfixrest.security.UserPrincipal;
import com.lyubenblagoev.postfixrest.service.model.AuditLogResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {

    private static final String SYSTEM_USER = "system";

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void recordCreated(AuditEntityType entityType, Long entityId, Map<String, ?> newValues) {
        record(AuditAction.CREATED, entityType, entityId, Map.of(), newValues, Set.of());
    }

    @Override
    @Transactional
    public void recordUpdated(AuditEntityType entityType, Long entityId, Map<String, ?> oldValues,
                              Map<String, ?> newValues) {
        recordUpdated(entityType, entityId, oldValues, newValues, Set.of());
    }

    @Override
    @Transactional
    public void recordUpdated(AuditEntityType entityType, Long entityId, Map<String, ?> oldValues,
                              Map<String, ?> newValues, Set<String> changedFields) {
        record(AuditAction.UPDATED, entityType, entityId, oldValues, newValues, changedFields);
    }

    @Override
    @Transactional
    public void recordDeleted(AuditEntityType entityType, Long entityId, Map<String, ?> oldValues) {
        record(AuditAction.DELETED, entityType, entityId, oldValues, Map.of(), Set.of());
    }

    @Override
    public List<AuditLogResource> find(AuditEntityType entityType, Long entityId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<AuditLog> logs;
        if (entityType != null && entityId != null) {
            logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByOccurredAtDescIdDesc(entityType, entityId,
                    pageRequest);
        } else if (entityType != null) {
            logs = auditLogRepository.findByEntityTypeOrderByOccurredAtDescIdDesc(entityType, pageRequest);
        } else if (entityId != null) {
            logs = auditLogRepository.findByEntityIdOrderByOccurredAtDescIdDesc(entityId, pageRequest);
        } else {
            logs = auditLogRepository.findAllByOrderByOccurredAtDescIdDesc(pageRequest);
        }
        return logs.stream().map(AuditLogResource::fromAuditLog).collect(Collectors.toList());
    }

    private void record(AuditAction action, AuditEntityType entityType, Long entityId,
                        Map<String, ?> oldValues, Map<String, ?> newValues, Set<String> changedFields) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setOccurredAt(new Date());
        setActor(auditLog);

        Set<String> fields = new LinkedHashSet<>();
        fields.addAll(oldValues.keySet());
        fields.addAll(newValues.keySet());
        for (String field : fields) {
            Object oldValue = oldValues.get(field);
            Object newValue = newValues.get(field);
            if (action == AuditAction.UPDATED && !changedFields.contains(field)
                    && Objects.equals(oldValue, newValue)) {
                continue;
            }
            auditLog.addChange(new AuditLogChange(field, formatValue(oldValue), formatValue(newValue)));
        }

        if (action != AuditAction.UPDATED || !auditLog.getChanges().isEmpty()) {
            auditLogRepository.save(auditLog);
        }
    }

    private void setActor(AuditLog auditLog) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            auditLog.setActorUsername(SYSTEM_USER);
            return;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            auditLog.setActorUserId(userPrincipal.getId());
            auditLog.setActorUsername(userPrincipal.getUsername());
        } else {
            auditLog.setActorUsername(authentication.getName());
        }
    }

    private String formatValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date date) {
            return date.toInstant().toString();
        }
        return String.valueOf(value);
    }
}
