package com.lyubenblagoev.postfixrest.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lyubenblagoev.postfixrest.entity.Domain;

public interface DomainRepository extends JpaRepository<Domain, Long> {

	Optional<Domain> findByName(String name);

	List<Domain> findAllByOrderByNameAsc();

	List<Domain> findAllByIdInOrderByNameAsc(Collection<Long> ids);

	Page<Domain> findByIdIn(Collection<Long> ids, Pageable pageable);

}
