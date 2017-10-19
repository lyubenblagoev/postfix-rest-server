package com.lyubenblagoev.postfixrest.service;

import java.util.Collection;

import com.lyubenblagoev.postfixrest.service.model.AliasChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AliasResource;

public interface AliasService {
	
	AliasResource getAlias(Long id);

	Collection<AliasResource> getAliasesByDomainName(String domainName);

	Collection<AliasResource> getAliasByDomainNameAndName(String domainName, String name);

	AliasResource getAliasByDomainNameAndNameAndEmail(String domainName, String name, String email);

	AliasResource save(AliasChangeRequest name);

	void delete(String name, String domain);

	void delete(String name, String domain, String email);
	
}
