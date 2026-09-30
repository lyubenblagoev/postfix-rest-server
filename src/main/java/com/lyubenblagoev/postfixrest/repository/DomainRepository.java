package com.lyubenblagoev.postfixrest.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import org.springframework.data.repository.CrudRepository;

import com.lyubenblagoev.postfixrest.entity.Domain;

public interface DomainRepository extends CrudRepository<Domain, Long> {

	Optional<Domain> findByName(String name);

	List<Domain> findAllByOrderByNameAsc();

	List<Domain> findAllByIdInOrderByNameAsc(Collection<Long> ids);

}
