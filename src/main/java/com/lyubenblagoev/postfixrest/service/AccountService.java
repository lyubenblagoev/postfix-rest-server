package com.lyubenblagoev.postfixrest.service;

import java.util.List;

import com.lyubenblagoev.postfixrest.service.model.AccountChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AccountResource;

public interface AccountService {
	AccountResource getAccountById(Long id);
	List<AccountResource> getAccountsByDomainName(String name);
	AccountResource getAccountByNameAndDomainName(String username, String domainName);
	AccountResource save(AccountChangeRequest user);
	void delete(String username, String domainName);
}
