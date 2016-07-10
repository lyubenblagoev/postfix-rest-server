package com.lyubenblagoev.postfixrest.repository;

import org.springframework.data.repository.CrudRepository;

import com.lyubenblagoev.postfixrest.entity.Domain;

public interface DomainRepository extends CrudRepository<Domain, Long> {

	Domain findByName(String name);

}
