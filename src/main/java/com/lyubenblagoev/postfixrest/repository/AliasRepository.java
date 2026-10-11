package com.lyubenblagoev.postfixrest.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lyubenblagoev.postfixrest.entity.Alias;

public interface AliasRepository extends JpaRepository<Alias, Long> {

	Collection<Alias> findByDomainId(Long domainId);

	Collection<Alias> findByDomainName(String domainName);

	Page<Alias> findByDomainNameOrderByAliasAscEmailAscIdAsc(String domainName, Pageable pageable);
	
	Collection<Alias> findByDomainNameAndAlias(String domainName, String alias);

	Page<Alias> findByDomainNameAndAliasOrderByEmailAscIdAsc(String domainName, String alias, Pageable pageable);
	
	Optional<Alias> findByDomainNameAndAliasAndEmail(String domainName, String alias, String email);

	@Query("""
			select a.domain.id as domainId,
			       a.alias as aliasName,
			       count(a.id) as recipientCount,
			       sum(case when a.enabled = true then 1 else 0 end) as enabledRecipients,
			       sum(case when a.enabled = false then 1 else 0 end) as disabledRecipients
			from Alias a
			group by a.domain.id, a.alias
			""")
	List<DomainAliasAggregate> findAggregatesByDomain();

	@Query("""
			select a.domain.id as domainId,
			       a.alias as aliasName,
			       count(a.id) as recipientCount,
			       sum(case when a.enabled = true then 1 else 0 end) as enabledRecipients,
			       sum(case when a.enabled = false then 1 else 0 end) as disabledRecipients
			from Alias a
			where a.domain.id = :domainId
			group by a.domain.id, a.alias
			""")
	List<DomainAliasAggregate> findAggregatesByDomainId(@Param("domainId") Long domainId);

	@Query("""
			select a.domain.id as domainId,
			       a.alias as aliasName,
			       count(a.id) as recipientCount,
			       sum(case when a.enabled = true then 1 else 0 end) as enabledRecipients,
			       sum(case when a.enabled = false then 1 else 0 end) as disabledRecipients
			from Alias a
			where a.domain.id in :domainIds
			group by a.domain.id, a.alias
			""")
	List<DomainAliasAggregate> findAggregatesByDomainIds(@Param("domainIds") Collection<Long> domainIds);

}
