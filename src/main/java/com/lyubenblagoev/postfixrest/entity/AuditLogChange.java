package com.lyubenblagoev.postfixrest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_log_changes")
public class AuditLogChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "audit_log_id", nullable = false)
    private AuditLog auditLog;

    @Column(name = "field_name", nullable = false, length = 128)
    private String fieldName;

    @Column(name = "old_value", length = 2048)
    private String oldValue;

    @Column(name = "new_value", length = 2048)
    private String newValue;

    public AuditLogChange() {
    }

    public AuditLogChange(String fieldName, String oldValue, String newValue) {
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Long getId() {
        return id;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

    public void setAuditLog(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }
}
