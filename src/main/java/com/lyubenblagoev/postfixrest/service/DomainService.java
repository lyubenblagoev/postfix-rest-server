package com.lyubenblagoev.postfixrest.service;

import java.util.List;

import com.lyubenblagoev.postfixrest.service.model.DomainResource;

public interface DomainService {
	List<DomainResource> getAllDomains();
	DomainResource getDomain(Long id);
	DomainResource getDomainByName(String name);
	DomainResource save(DomainResource domain);
	void delete(Long id);
}
