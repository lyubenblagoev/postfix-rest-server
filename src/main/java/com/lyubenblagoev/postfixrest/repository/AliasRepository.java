package com.lyubenblagoev.postfixrest.repository;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;

import com.lyubenblagoev.postfixrest.entity.Alias;

public interface AliasRepository extends CrudRepository<Alias, Long> {

	Collection<Alias> findByDomainId(Long domainId);

	Collection<Alias> findByDomainName(String domainName);
	
	Collection<Alias> findByDomainNameAndAlias(String domainName, String alias);
	
	Alias findByDomainNameAndAliasAndEmail(String domainName, String alias, String email);

}
