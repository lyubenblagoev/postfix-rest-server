package com.lyubenblagoev.postfixrest.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.lyubenblagoev.postfixrest.entity.Alias;

public interface AliasRepository extends CrudRepository<Alias, Long> {

	List<Alias> findByDomainId(Long domainId);

	List<Alias> findByDomainName(String domainName);
	
	Alias findByDomainNameAndAlias(String domainName, String alias);

}
