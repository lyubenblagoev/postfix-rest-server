package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.entity.AuditAction;
import com.lyubenblagoev.postfixrest.entity.AuditEntityType;
import com.lyubenblagoev.postfixrest.entity.AuditLog;
import com.lyubenblagoev.postfixrest.entity.AuditLogChange;
import com.lyubenblagoev.postfixrest.entity.AuditLogScopeBackfillState;
import com.lyubenblagoev.postfixrest.repository.AuditLogRepository;
import com.lyubenblagoev.postfixrest.repository.AuditLogScopeBackfillStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AuditLogScopeBackfill implements ApplicationRunner {

    private static final Long STATE_ID = 1L;
    private static final int BACKFILL_VERSION = 2;
    private static final Logger logger = LoggerFactory.getLogger(AuditLogScopeBackfill.class);

    private final AuditLogRepository auditLogRepository;
    private final AuditLogScopeBackfillStateRepository stateRepository;

    public AuditLogScopeBackfill(AuditLogRepository auditLogRepository,
                                 AuditLogScopeBackfillStateRepository stateRepository) {
        this.auditLogRepository = auditLogRepository;
        this.stateRepository = stateRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Optional<AuditLogScopeBackfillState> existingState = stateRepository.findById(STATE_ID);
        if (existingState.isPresent() && existingState.get().getVersion() >= BACKFILL_VERSION) {
            return;
        }

        List<AuditLog> logs = auditLogRepository.findAll();
        Map<Long, List<AccountScopeEvent>> accountHistory = accountHistory(logs);
        Map<AuditEntityType, Map<Long, Long>> bccAccountIds = bccAccountIds(logs);
        List<AuditLog> updatedLogs = new ArrayList<>();

        for (AuditLog log : logs) {
            boolean updated = false;
            Long accountId = log.getScopeAccountId();
            if (accountId == null) {
                accountId = snapshotValue(log, "accountId");
                if (accountId == null && log.getEntityType() == AuditEntityType.ACCOUNT) {
                    accountId = log.getEntityId();
                }
                if (accountId == null && isBcc(log)) {
                    accountId = bccAccountIds.getOrDefault(log.getEntityType(), Map.of()).get(log.getEntityId());
                }
                if (accountId != null) {
                    log.setScopeAccountId(accountId);
                    updated = true;
                }
            }

            if (log.getScopeDomainId() == null) {
                Long domainId = snapshotValue(log, "domainId");
                if (domainId == null && log.getEntityType() == AuditEntityType.DOMAIN) {
                    domainId = log.getEntityId();
                }
                if (domainId == null && accountId != null) {
                    domainId = historicalAccountDomain(accountHistory.get(accountId), log);
                }
                if (domainId != null) {
                    log.setScopeDomainId(domainId);
                    updated = true;
                }
            }

            if (log.getResourceIdentifier() == null) {
                String resourceIdentifier = resourceIdentifier(log);
                if (resourceIdentifier != null) {
                    log.setResourceIdentifier(resourceIdentifier);
                    updated = true;
                }
            }

            if (updated) {
                updatedLogs.add(log);
            }
        }

        if (!updatedLogs.isEmpty()) {
            auditLogRepository.saveAll(updatedLogs);
            logger.info("Backfilled scope metadata for {} audit log records", updatedLogs.size());
        }

        AuditLogScopeBackfillState state = existingState.orElseGet(AuditLogScopeBackfillState::new);
        state.setId(STATE_ID);
        state.setVersion(BACKFILL_VERSION);
        state.setCompletedAt(new Date());
        stateRepository.save(state);
        logger.info("Completed audit-log scope backfill version {}", BACKFILL_VERSION);
    }

    private Map<Long, List<AccountScopeEvent>> accountHistory(List<AuditLog> logs) {
        Map<Long, List<AccountScopeEvent>> history = new HashMap<>();
        for (AuditLog log : logs) {
            if (log.getEntityType() != AuditEntityType.ACCOUNT) {
                continue;
            }
            Long domainId = snapshotValue(log, "domainId");
            if (domainId != null) {
                history.computeIfAbsent(log.getEntityId(), ignored -> new ArrayList<>())
                        .add(new AccountScopeEvent(log.getOccurredAt(), log.getId(), domainId));
            }
        }
        history.values().forEach(events -> events.sort(Comparator
                .comparing(AccountScopeEvent::occurredAt)
                .thenComparing(AccountScopeEvent::id, Comparator.nullsFirst(Long::compareTo))));
        return history;
    }

    private Map<AuditEntityType, Map<Long, Long>> bccAccountIds(List<AuditLog> logs) {
        Map<AuditEntityType, Map<Long, Long>> accountIds = new HashMap<>();
        for (AuditLog log : logs) {
            if (isBcc(log)) {
                Long accountId = snapshotValue(log, "accountId");
                if (accountId != null) {
                    accountIds.computeIfAbsent(log.getEntityType(), ignored -> new HashMap<>())
                            .put(log.getEntityId(), accountId);
                }
            }
        }
        return accountIds;
    }

    private boolean isBcc(AuditLog log) {
        return log.getEntityType() == AuditEntityType.INCOMING_BCC
                || log.getEntityType() == AuditEntityType.OUTGOING_BCC;
    }

    private Long historicalAccountDomain(List<AccountScopeEvent> history, AuditLog bccLog) {
        if (history == null) {
            return null;
        }
        Long domainId = null;
        for (AccountScopeEvent event : history) {
            if (isAtOrBefore(event, bccLog)) {
                domainId = event.domainId();
            } else {
                break;
            }
        }
        return domainId;
    }

    private boolean isAtOrBefore(AccountScopeEvent event, AuditLog log) {
        int dateComparison = event.occurredAt().compareTo(log.getOccurredAt());
        if (dateComparison != 0) {
            return dateComparison < 0;
        }
        return event.id() == null || log.getId() == null || event.id() <= log.getId();
    }

    private Long snapshotValue(AuditLog log, String fieldName) {
        for (AuditLogChange change : log.getChanges()) {
            if (!fieldName.equals(change.getFieldName())) {
                continue;
            }
            String value = log.getAction() == AuditAction.DELETED ? change.getOldValue() : change.getNewValue();
            if (value == null) {
                value = change.getOldValue();
            }
            return parseLong(value);
        }
        return null;
    }

    private String resourceIdentifier(AuditLog log) {
        return switch (log.getEntityType()) {
            case DOMAIN -> snapshotText(log, "name");
            case ACCOUNT -> mailboxIdentifier(log);
            case ALIAS -> aliasIdentifier(log);
            case INCOMING_BCC, OUTGOING_BCC -> snapshotText(log, "email");
            case USER -> snapshotText(log, "email");
        };
    }

    private String mailboxIdentifier(AuditLog log) {
        String username = snapshotText(log, "username");
        String domainName = snapshotText(log, "domainName");
        return username == null || domainName == null ? null : username + "@" + domainName;
    }

    private String aliasIdentifier(AuditLog log) {
        String aliasName = snapshotText(log, "name");
        String domainName = snapshotText(log, "domainName");
        String recipient = snapshotText(log, "email");
        return aliasName == null || domainName == null || recipient == null
                ? null : aliasName + "@" + domainName + " -> " + recipient;
    }

    private String snapshotText(AuditLog log, String fieldName) {
        for (AuditLogChange change : log.getChanges()) {
            if (!fieldName.equals(change.getFieldName())) {
                continue;
            }
            String value = log.getAction() == AuditAction.DELETED
                    ? change.getOldValue() : change.getNewValue();
            return value == null ? change.getOldValue() : value;
        }
        return null;
    }

    private Long parseLong(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private record AccountScopeEvent(Date occurredAt, Long id, Long domainId) {
    }
}
