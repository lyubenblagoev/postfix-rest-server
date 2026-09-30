package com.lyubenblagoev.postfixrest.repository;

import com.lyubenblagoev.postfixrest.entity.WorkspaceInvitation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from WorkspaceInvitation i where i.tokenHash = :tokenHash")
    Optional<WorkspaceInvitation> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    void deleteByWorkspaceId(Long workspaceId);
}
