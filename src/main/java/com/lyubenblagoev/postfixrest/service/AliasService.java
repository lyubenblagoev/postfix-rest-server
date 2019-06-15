package com.lyubenblagoev.postfixrest.service;

import java.util.Collection;
import java.util.Optional;

import com.lyubenblagoev.postfixrest.service.model.AliasChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AliasResource;

public interface AliasService {
	
	Optional<AliasResource> getAlias(Long id);

	Collection<AliasResource> getAliasesByDomainName(String domainName);

	Collection<AliasResource> getAliasByDomainNameAndName(String domainName, String name);

	Optional<AliasResource> getAliasByDomainNameAndNameAndEmail(String domainName, String name, String email);

	Optional<AliasResource> save(AliasChangeRequest name);

	void delete(String name, String domain);

	void delete(String name, String domain, String email);
	
}
