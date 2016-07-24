package com.lyubenblagoev.postfixrest.service;

import java.util.List;

import com.lyubenblagoev.postfixrest.service.model.DomainResource;

public interface DomainService {
	List<DomainResource> getAllDomains();
	DomainResource getDomainByName(String name);
	DomainResource save(DomainResource domain);
	void delete(String domainName);
}
