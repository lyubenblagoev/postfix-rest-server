package com.lyubenblagoev.postfixrest.service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyubenblagoev.postfixrest.entity.AuditAction;
import com.lyubenblagoev.postfixrest.entity.AuditEntityType;
import com.lyubenblagoev.postfixrest.entity.AuditLog;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLogResource {

    private Long id;
    private AuditAction action;
    private AuditEntityType entityType;
    private Long entityId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date timestamp;

    private Long userId;
    private String user;
    private List<AuditFieldChangeResource> changes;

    public static AuditLogResource fromAuditLog(AuditLog auditLog) {
        AuditLogResource resource = new AuditLogResource();
        resource.id = auditLog.getId();
        resource.action = auditLog.getAction();
        resource.entityType = auditLog.getEntityType();
        resource.entityId = auditLog.getEntityId();
        resource.timestamp = auditLog.getOccurredAt();
        resource.userId = auditLog.getActorUserId();
        resource.user = auditLog.getActorUsername();
        resource.changes = auditLog.getChanges().stream()
                .map(change -> new AuditFieldChangeResource(change.getFieldName(), change.getOldValue(),
                        change.getNewValue()))
                .collect(Collectors.toList());
        return resource;
    }

    public Long getId() {
        return id;
    }

    public AuditAction getAction() {
        return action;
    }

    public AuditEntityType getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUser() {
        return user;
    }

    public List<AuditFieldChangeResource> getChanges() {
        return changes;
    }
}
