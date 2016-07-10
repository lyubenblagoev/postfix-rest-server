package com.lyubenblagoev.postfixrest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lyubenblagoev.postfixrest.service.AccountService;
import com.lyubenblagoev.postfixrest.service.model.AccountChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AccountResource;

@RestController
@RequestMapping("/api/v1/domains/{domainId}/accounts")
public class AccountController {
	
	@Autowired
	private AccountService accountService;
	
	@RequestMapping(method = RequestMethod.GET)
	public List<AccountResource> getAccounts(@PathVariable("domainId") Long domainId) {
		return accountService.getAccountsByDomainId(domainId);
	}

	@RequestMapping(method = RequestMethod.POST)
	public void addAccount(@PathVariable("domainId") Long domainId, @Validated @RequestBody AccountChangeRequest account, BindingResult result) {
		String error = result.hasErrors() ? result.getFieldError().toString() : null;
		saveAccount(domainId, account, error);
	}
	
	@RequestMapping(value = "/{accountId}", method = RequestMethod.GET)
	public AccountResource getAccount(@PathVariable("domainId") Long domainId, @PathVariable("accountId") Long accountId) {
		return accountService.getAccountById(accountId);
		
	}

	@RequestMapping(value = "/{accountId}", method = RequestMethod.DELETE)
	public void deleteAccount(@PathVariable("domainId") Long domainId, @PathVariable("accountId") Long accountId) {
		accountService.delete(accountId);
	}
	
	@RequestMapping(value = "/{accountId}", method = RequestMethod.PUT)
	public void edit(@PathVariable("domainId") Long domainId, @PathVariable("accountId") Long accountId, 
			@Validated @RequestBody AccountChangeRequest account, BindingResult result) {
		String error = result.hasErrors() ? result.getFieldError().toString() : null;
		account.setId(accountId);
		saveAccount(domainId, account, error);
	}
	
	private void saveAccount(Long domainId, AccountChangeRequest account, String error) {
		if (error != null) {
			throw new AccountException(error);
		}
		if (account.getPassword() != null && !account.getPassword().equals(account.getConfirmPassword())) {
			throw new AccountException("passwords don't match");
		}
		account.setDomainId(domainId);
		accountService.save(account);
	}

}
