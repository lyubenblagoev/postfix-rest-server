package com.lyubenblagoev.postfixrest.repository;

import com.lyubenblagoev.postfixrest.entity.Workspace;
import com.lyubenblagoev.postfixrest.entity.WorkspaceMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMappingRepository extends JpaRepository<WorkspaceMapping, Long> {

    @Query("select distinct d.id from WorkspaceMapping m join m.workspace w join w.domains d " +
            "where m.user.id = :userId and m.enabled = true")
    List<Long> findAccessibleDomainIds(@Param("userId") Long userId);

    @Query("select case when count(m) > 0 then true else false end from WorkspaceMapping m " +
            "join m.workspace w join w.domains d where m.user.id = :userId and d.id = :domainId " +
            "and m.enabled = true")
    boolean hasDomainAccess(@Param("userId") Long userId, @Param("domainId") Long domainId);

    @Query("select m from WorkspaceMapping m join fetch m.workspace w join fetch m.role " +
            "where m.user.id = :userId order by w.name")
    List<WorkspaceMapping> findByUserIdWithWorkspace(@Param("userId") Long userId);

    @Query("select distinct w from WorkspaceMapping m join m.workspace w join m.role r " +
            "left join fetch w.domains where m.user.id = :userId and m.enabled = true " +
            "and r.name = :roleName order by w.name")
    List<Workspace> findEnabledWorkspacesByUserIdAndRoleName(@Param("userId") Long userId,
                                                             @Param("roleName") String roleName);

    @Query(value = "select distinct w from WorkspaceMapping m join m.workspace w join m.role r " +
            "where m.user.id = :userId and m.enabled = true and r.name = :roleName " +
            "order by w.name asc, w.id asc",
            countQuery = "select count(distinct w.id) from WorkspaceMapping m join m.workspace w join m.role r " +
                    "where m.user.id = :userId and m.enabled = true and r.name = :roleName")
    Page<Workspace> findPageOfEnabledWorkspacesByUserIdAndRoleName(@Param("userId") Long userId,
                                                                   @Param("roleName") String roleName,
                                                                   Pageable pageable);

    @Query("select distinct w from WorkspaceMapping m join m.workspace w left join fetch w.domains " +
            "where m.user.id = :userId and m.enabled = true order by w.name")
    List<Workspace> findEnabledWorkspacesByUserId(@Param("userId") Long userId);

    @Query(value = "select distinct w from WorkspaceMapping m join m.workspace w " +
            "where m.user.id = :userId and m.enabled = true order by w.name asc, w.id asc",
            countQuery = "select count(distinct w.id) from WorkspaceMapping m join m.workspace w " +
                    "where m.user.id = :userId and m.enabled = true")
    Page<Workspace> findPageOfEnabledWorkspacesByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("select m from WorkspaceMapping m join fetch m.user join fetch m.role " +
            "where m.workspace.id = :workspaceId order by m.user.email")
    List<WorkspaceMapping> findByWorkspaceIdWithUser(@Param("workspaceId") Long workspaceId);

    @Query(value = "select m from WorkspaceMapping m join fetch m.user join fetch m.role " +
            "where m.workspace.id = :workspaceId order by m.user.email asc, m.id asc",
            countQuery = "select count(m) from WorkspaceMapping m where m.workspace.id = :workspaceId")
    Page<WorkspaceMapping> findPageByWorkspaceIdWithUser(@Param("workspaceId") Long workspaceId,
                                                         Pageable pageable);

    Optional<WorkspaceMapping> findByUserIdAndWorkspaceId(Long userId, Long workspaceId);

    @Query("select m from WorkspaceMapping m join fetch m.role where m.user.id = :userId " +
            "and m.workspace.id = :workspaceId and m.enabled = true")
    Optional<WorkspaceMapping> findEnabledByUserIdAndWorkspaceId(@Param("userId") Long userId,
                                                                 @Param("workspaceId") Long workspaceId);

    void deleteByUserIdAndWorkspaceId(Long userId, Long workspaceId);
}
