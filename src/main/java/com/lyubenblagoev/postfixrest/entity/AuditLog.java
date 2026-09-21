package com.lyubenblagoev.postfixrest.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_entity_history", columnList = "entity_type, entity_id, occurred_at"),
        @Index(name = "idx_audit_recent", columnList = "occurred_at"),
        @Index(name = "idx_audit_domain_scope", columnList = "domain_id, occurred_at"),
        @Index(name = "idx_audit_account_scope", columnList = "account_id, occurred_at")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 32)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 32)
    private AuditEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    // Historical identifier captured when the audited resource was changed; does not depend on current resources.
    @Column(name = "resource_identifier", length = 1024)
    private String resourceIdentifier;

    // Parent account used to include this entry in account-scoped activity; not the audited resource ID.
    @Column(name = "account_id")
    private Long scopeAccountId;

    // Parent domain used to include this entry in domain-scoped activity; not the audited resource ID.
    @Column(name = "domain_id")
    private Long scopeDomainId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "occurred_at", nullable = false)
    private Date occurredAt;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_username", nullable = false, length = 320)
    private String actorUsername;

    @OneToMany(mappedBy = "auditLog", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<AuditLogChange> changes = new ArrayList<>();

    public void addChange(AuditLogChange change) {
        changes.add(change);
        change.setAuditLog(this);
    }

    public Long getId() {
        return id;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public AuditEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(AuditEntityType entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public void setResourceIdentifier(String resourceIdentifier) {
        this.resourceIdentifier = resourceIdentifier;
    }

    public Long getScopeAccountId() {
        return scopeAccountId;
    }

    public void setScopeAccountId(Long scopeAccountId) {
        this.scopeAccountId = scopeAccountId;
    }

    public Long getScopeDomainId() {
        return scopeDomainId;
    }

    public void setScopeDomainId(Long scopeDomainId) {
        this.scopeDomainId = scopeDomainId;
    }

    public Date getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Date occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(Long actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public List<AuditLogChange> getChanges() {
        return changes;
    }
}
