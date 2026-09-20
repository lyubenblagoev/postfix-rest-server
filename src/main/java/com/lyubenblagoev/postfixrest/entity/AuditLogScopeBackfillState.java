package com.lyubenblagoev.postfixrest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@Entity
@Table(name = "audit_log_scope_backfill")
public class AuditLogScopeBackfillState {

    @Id
    private Long id;

    @Column(name = "backfill_version", nullable = false)
    private int version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "completed_at", nullable = false)
    private Date completedAt;

    public AuditLogScopeBackfillState() {
    }

    public AuditLogScopeBackfillState(Long id, int version, Date completedAt) {
        this.id = id;
        this.version = version;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }
}
