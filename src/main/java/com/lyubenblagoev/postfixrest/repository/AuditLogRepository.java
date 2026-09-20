package com.lyubenblagoev.postfixrest.repository;

import com.lyubenblagoev.postfixrest.entity.AuditEntityType;
import com.lyubenblagoev.postfixrest.entity.AuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByOccurredAtDescIdDesc(AuditEntityType entityType,
                                                                          Long entityId, Pageable pageable);

    List<AuditLog> findByEntityTypeOrderByOccurredAtDescIdDesc(AuditEntityType entityType, Pageable pageable);

    List<AuditLog> findByEntityIdOrderByOccurredAtDescIdDesc(Long entityId, Pageable pageable);

    List<AuditLog> findAllByOrderByOccurredAtDescIdDesc(Pageable pageable);

    List<AuditLog> findByScopeDomainIdAndEntityTypeInOrderByOccurredAtDescIdDesc(Long scopeDomainId,
                                                                                   Collection<AuditEntityType> entityTypes,
                                                                                   Pageable pageable);

    List<AuditLog> findByScopeAccountIdAndEntityTypeInOrderByOccurredAtDescIdDesc(Long scopeAccountId,
                                                                                   Collection<AuditEntityType> entityTypes,
                                                                                   Pageable pageable);
}
