package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.entity.AuditAction;
import com.lyubenblagoev.postfixrest.entity.AuditEntityType;
import com.lyubenblagoev.postfixrest.entity.AuditLog;
import com.lyubenblagoev.postfixrest.entity.AuditLogChange;
import com.lyubenblagoev.postfixrest.entity.AuditLogScopeBackfillState;
import com.lyubenblagoev.postfixrest.repository.AuditLogRepository;
import com.lyubenblagoev.postfixrest.repository.AuditLogScopeBackfillStateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogScopeBackfillTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogScopeBackfillStateRepository stateRepository;

    @Test
    void run_whenLegacyDeletedBccExists_shouldRestoreAccountAndDomainScopes() throws Exception {
        AuditLog accountLog = auditLog(AuditAction.CREATED, AuditEntityType.ACCOUNT, 42L, 1_000L);
        accountLog.addChange(new AuditLogChange("domainId", null, "7"));

        AuditLog bccLog = auditLog(AuditAction.DELETED, AuditEntityType.OUTGOING_BCC, 99L, 2_000L);
        bccLog.addChange(new AuditLogChange("accountId", "42", null));
        bccLog.addChange(new AuditLogChange("email", "bcc@example.net", null));

        when(auditLogRepository.findAll()).thenReturn(List.of(accountLog, bccLog));
        when(stateRepository.findById(1L)).thenReturn(Optional.empty());

        new AuditLogScopeBackfill(auditLogRepository, stateRepository).run(null);

        assertEquals(42L, bccLog.getScopeAccountId());
        assertEquals(7L, bccLog.getScopeDomainId());
        assertEquals("bcc@example.net", bccLog.getResourceIdentifier());
        verify(auditLogRepository).saveAll(anyList());
        verify(stateRepository).save(org.mockito.ArgumentMatchers.any(AuditLogScopeBackfillState.class));
    }

    @Test
    void run_whenCurrentVersionAlreadyCompleted_shouldSkipFullBackfill() throws Exception {
        when(stateRepository.findById(1L))
                .thenReturn(Optional.of(new AuditLogScopeBackfillState(1L, 2, new Date())));

        new AuditLogScopeBackfill(auditLogRepository, stateRepository).run(null);

        verifyNoInteractions(auditLogRepository);
    }

    @Test
    void run_whenBccDomainCannotBeRecovered_shouldLeaveDomainScopeUnset() throws Exception {
        AuditLog bccLog = auditLog(AuditAction.DELETED, AuditEntityType.OUTGOING_BCC, 99L, 2_000L);
        bccLog.addChange(new AuditLogChange("accountId", "42", null));
        bccLog.addChange(new AuditLogChange("email", "bcc@example.net", null));

        when(auditLogRepository.findAll()).thenReturn(List.of(bccLog));
        when(stateRepository.findById(1L)).thenReturn(Optional.empty());

        new AuditLogScopeBackfill(auditLogRepository, stateRepository).run(null);

        assertEquals(42L, bccLog.getScopeAccountId());
        org.junit.jupiter.api.Assertions.assertNull(bccLog.getScopeDomainId());
        assertEquals("bcc@example.net", bccLog.getResourceIdentifier());
    }

    private AuditLog auditLog(AuditAction action, AuditEntityType entityType, Long entityId, long timestamp) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOccurredAt(new Date(timestamp));
        log.setActorUsername("system");
        return log;
    }
}
