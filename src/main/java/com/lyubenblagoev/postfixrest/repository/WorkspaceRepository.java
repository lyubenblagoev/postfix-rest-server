package com.lyubenblagoev.postfixrest.repository;

import com.lyubenblagoev.postfixrest.entity.Workspace;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Optional<Workspace> findByName(String name);

    @Query("select distinct w from Workspace w left join fetch w.domains where w.id in :workspaceIds")
    List<Workspace> findAllWithDomainsByIdIn(@Param("workspaceIds") Collection<Long> workspaceIds);
}
