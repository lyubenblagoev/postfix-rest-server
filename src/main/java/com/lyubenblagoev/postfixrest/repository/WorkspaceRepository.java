package com.lyubenblagoev.postfixrest.repository;

import com.lyubenblagoev.postfixrest.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Optional<Workspace> findByName(String name);
}
