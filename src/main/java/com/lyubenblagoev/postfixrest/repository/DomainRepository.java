package com.lyubenblagoev.postfixrest.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.lyubenblagoev.postfixrest.entity.Domain;

public interface DomainRepository extends CrudRepository<Domain, Long> {

	Optional<Domain> findByName(String name);

}
