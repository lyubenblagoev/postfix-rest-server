package com.lyubenblagoev.postfixrest.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.lyubenblagoev.postfixrest.entity.Account;

public interface AccountRepository extends CrudRepository<Account, Long> {

	List<Account> findByDomainName(String name);

	Account findByUsernameAndDomainName(String username, String domainName);

}
