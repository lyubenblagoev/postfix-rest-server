package com.lyubenblagoev.postfixrest.repository;

import com.lyubenblagoev.postfixrest.entity.AuditLogScopeBackfillState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogScopeBackfillStateRepository extends JpaRepository<AuditLogScopeBackfillState, Long> {
}
