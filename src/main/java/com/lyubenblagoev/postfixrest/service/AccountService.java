package com.lyubenblagoev.postfixrest.service;

import java.util.List;

import com.lyubenblagoev.postfixrest.service.model.AccountChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AccountResource;

public interface AccountService {
	List<AccountResource> getAllAccounts();
	AccountResource getAccountById(Long id);
	List<AccountResource> getAccountsByDomainId(Long domainId);
	AccountResource save(AccountChangeRequest user);
	void delete(Long id);
}
