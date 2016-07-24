package com.lyubenblagoev.postfixrest.service;

import java.util.List;

import com.lyubenblagoev.postfixrest.service.model.AliasChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AliasResource;

public interface AliasService {
	AliasResource getAlias(Long id);
	List<AliasResource> getAliasesByDomainName(String domainName);
	AliasResource getAliasByDomainNameAndAlias(String domainName, String alias);
	AliasResource save(AliasChangeRequest alias);
	void delete(String alias, String domain);
}
