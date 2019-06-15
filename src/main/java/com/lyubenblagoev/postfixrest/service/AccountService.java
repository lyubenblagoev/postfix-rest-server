package com.lyubenblagoev.postfixrest.service;

import java.util.List;
import java.util.Optional;

import com.lyubenblagoev.postfixrest.service.model.AccountChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AccountResource;

public interface AccountService {
	
	Optional<AccountResource> getAccountById(Long id);

	List<AccountResource> getAccountsByDomainName(String name);

	Optional<AccountResource> getAccountByNameAndDomainName(String username, String domainName);

	Optional<AccountResource> save(AccountChangeRequest user);

	void delete(AccountResource account);
	
}
