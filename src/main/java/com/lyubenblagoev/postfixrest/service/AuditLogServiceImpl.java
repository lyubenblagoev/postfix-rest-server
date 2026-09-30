package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.entity.AuditAction;
import com.lyubenblagoev.postfixrest.entity.AuditEntityType;
import com.lyubenblagoev.postfixrest.entity.AuditLog;
import com.lyubenblagoev.postfixrest.entity.AuditLogChange;
import com.lyubenblagoev.postfixrest.entity.AuditScope;
import com.lyubenblagoev.postfixrest.UnauthorizedException;
import com.lyubenblagoev.postfixrest.repository.AuditLogRepository;
import com.lyubenblagoev.postfixrest.security.UserPrincipal;
import com.lyubenblagoev.postfixrest.service.model.AuditLogResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Collection;
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
    private static final String DOMAIN_NAME_FIELD = "domainName";
    private static final List<AuditEntityType> DOMAIN_ACTIVITY_TYPES = List.of(
            AuditEntityType.ACCOUNT,
            AuditEntityType.ALIAS,
            AuditEntityType.INCOMING_BCC,
            AuditEntityType.OUTGOING_BCC);
    private static final List<AuditEntityType> ACCOUNT_ACTIVITY_TYPES = List.of(
            AuditEntityType.ACCOUNT,
            AuditEntityType.INCOMING_BCC,
            AuditEntityType.OUTGOING_BCC);

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

    @Override
    public List<AuditLogResource> find(AuditEntityType entityType, Long entityId, int limit,
                                       Collection<Long> accessibleDomainIds) {
        if (accessibleDomainIds == null) {
            return find(entityType, entityId, limit);
        }
        if (accessibleDomainIds.isEmpty()) {
            return List.of();
        }
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<AuditLog> logs;
        if (entityType != null && entityId != null) {
            logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByOccurredAtDescIdDesc(entityType, entityId,
                    pageRequest);
            rejectOutOfScopeLogs(logs, accessibleDomainIds);
        } else if (entityType != null) {
            logs = auditLogRepository.findByScopeDomainIdInAndEntityTypeOrderByOccurredAtDescIdDesc(
                    accessibleDomainIds, entityType, pageRequest);
        } else if (entityId != null) {
            logs = auditLogRepository.findByEntityIdOrderByOccurredAtDescIdDesc(entityId, pageRequest);
            rejectOutOfScopeLogs(logs, accessibleDomainIds);
        } else {
            logs = auditLogRepository.findByScopeDomainIdInOrderByOccurredAtDescIdDesc(
                    accessibleDomainIds, pageRequest);
        }
        return logs.stream().map(AuditLogResource::fromAuditLog).collect(Collectors.toList());
    }

    @Override
    public List<AuditLogResource> find(AuditScope scope, Long scopeId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<AuditEntityType> entityTypes = scope == AuditScope.DOMAIN
                ? DOMAIN_ACTIVITY_TYPES : ACCOUNT_ACTIVITY_TYPES;
        List<AuditLog> logs = scope == AuditScope.DOMAIN
                ? auditLogRepository.findByScopeDomainIdAndEntityTypeInOrderByOccurredAtDescIdDesc(scopeId,
                        entityTypes, pageRequest)
                : auditLogRepository.findByScopeAccountIdAndEntityTypeInOrderByOccurredAtDescIdDesc(scopeId,
                        entityTypes, pageRequest);
        return logs.stream().map(AuditLogResource::fromAuditLog).collect(Collectors.toList());
    }

    private void record(AuditAction action, AuditEntityType entityType, Long entityId,
                        Map<String, ?> oldValues, Map<String, ?> newValues, Set<String> changedFields) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setResourceIdentifier(resourceIdentifier(action, entityType, oldValues, newValues));
        auditLog.setScopeAccountId(scopeId(entityType, entityId, "accountId", oldValues, newValues));
        auditLog.setScopeDomainId(scopeId(entityType, entityId, "domainId", oldValues, newValues));
        auditLog.setOccurredAt(new Date());
        setActor(auditLog);

        Set<String> fields = new LinkedHashSet<>();
        fields.addAll(oldValues.keySet());
        fields.addAll(newValues.keySet());
        for (String field : fields) {
            if (DOMAIN_NAME_FIELD.equals(field)) {
                continue;
            }
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

    private void rejectOutOfScopeLogs(List<AuditLog> logs, Collection<Long> accessibleDomainIds) {
        if (logs.stream().anyMatch(log -> log.getScopeDomainId() == null
                || !accessibleDomainIds.contains(log.getScopeDomainId()))) {
            throw new UnauthorizedException("Audit log access denied");
        }
    }

    private Long scopeId(AuditEntityType entityType, Long entityId, String field,
                         Map<String, ?> oldValues, Map<String, ?> newValues) {
        Object value = newValues.containsKey(field) ? newValues.get(field) : oldValues.get(field);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (entityType == AuditEntityType.ACCOUNT && "accountId".equals(field)) {
            return entityId;
        }
        if (entityType == AuditEntityType.DOMAIN && "domainId".equals(field)) {
            return entityId;
        }
        return null;
    }

    private String resourceIdentifier(AuditAction action, AuditEntityType entityType,
                                      Map<String, ?> oldValues, Map<String, ?> newValues) {
        Map<String, ?> values = action == AuditAction.DELETED ? oldValues : newValues;
        return switch (entityType) {
            case DOMAIN -> stringValue(values, "name");
            case ACCOUNT -> mailboxIdentifier(values);
            case ALIAS -> aliasIdentifier(values);
            case INCOMING_BCC, OUTGOING_BCC -> stringValue(values, "email");
            case USER -> stringValue(values, "email");
        };
    }

    private String mailboxIdentifier(Map<String, ?> values) {
        String username = stringValue(values, "username");
        String domainName = stringValue(values, DOMAIN_NAME_FIELD);
        return username == null || domainName == null ? null : username + "@" + domainName;
    }

    private String aliasIdentifier(Map<String, ?> values) {
        String aliasName = stringValue(values, "name");
        String domainName = stringValue(values, DOMAIN_NAME_FIELD);
        String recipient = stringValue(values, "email");
        return aliasName == null || domainName == null || recipient == null
                ? null : aliasName + "@" + domainName + " -> " + recipient;
    }

    private String stringValue(Map<String, ?> values, String fieldName) {
        Object value = values.get(fieldName);
        return value == null ? null : String.valueOf(value);
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
