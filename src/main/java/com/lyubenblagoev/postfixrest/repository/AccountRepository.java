package com.lyubenblagoev.postfixrest.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lyubenblagoev.postfixrest.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

	@Query("select a.domain.id from Account a where a.id = :accountId")
	Optional<Long> findDomainIdById(@Param("accountId") Long accountId);

	List<Account> findByDomainName(String name);

	Page<Account> findByDomainNameOrderByUsernameAscIdAsc(String name, Pageable pageable);

	Optional<Account> findByUsernameAndDomainName(String username, String domainName);

	@Query("""
			select a.domain.id as domainId,
			       count(a.id) as total,
			       sum(case when a.enabled = true then 1 else 0 end) as enabled,
			       sum(case when a.enabled = false then 1 else 0 end) as disabled
			from Account a
			group by a.domain.id
			""")
	List<DomainAccountAggregate> findAggregatesByDomain();

	@Query("""
			select a.domain.id as domainId,
			       count(a.id) as total,
			       sum(case when a.enabled = true then 1 else 0 end) as enabled,
			       sum(case when a.enabled = false then 1 else 0 end) as disabled
			from Account a
			where a.domain.id = :domainId
			group by a.domain.id
			""")
	List<DomainAccountAggregate> findAggregatesByDomainId(@Param("domainId") Long domainId);

	@Query("""
			select a.domain.id as domainId,
			       count(a.id) as total,
			       sum(case when a.enabled = true then 1 else 0 end) as enabled,
			       sum(case when a.enabled = false then 1 else 0 end) as disabled
			from Account a
			where a.domain.id in :domainIds
			group by a.domain.id
			""")
	List<DomainAccountAggregate> findAggregatesByDomainIds(@Param("domainIds") Collection<Long> domainIds);

}
