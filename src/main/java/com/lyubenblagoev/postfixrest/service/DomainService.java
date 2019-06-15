package com.lyubenblagoev.postfixrest.service;

import java.util.List;
import java.util.Optional;

import com.lyubenblagoev.postfixrest.service.model.DomainResource;

public interface DomainService {
	
	List<DomainResource> getAllDomains();

	Optional<DomainResource> getDomainByName(String name);

	Optional<DomainResource> save(DomainResource domain);

	void delete(DomainResource domain);
	
}
