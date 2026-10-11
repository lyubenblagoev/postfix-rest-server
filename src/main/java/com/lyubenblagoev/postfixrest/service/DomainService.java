package com.lyubenblagoev.postfixrest.service;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.lyubenblagoev.postfixrest.service.model.DomainResource;

public interface DomainService {
	
	List<DomainResource> getAllDomains();

	List<DomainResource> getAllDomains(Collection<Long> domainIds);

	Page<DomainResource> getAllDomains(Collection<Long> domainIds, Pageable pageable);

	Optional<DomainResource> getDomainByName(String name);

	Optional<DomainResource> save(DomainResource domain);

	void delete(DomainResource domain);
	
}
