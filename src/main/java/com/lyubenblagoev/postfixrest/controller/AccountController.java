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
import com.lyubenblagoev.postfixrest.service.DomainService;
import com.lyubenblagoev.postfixrest.service.model.AccountChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AccountResource;
import com.lyubenblagoev.postfixrest.service.model.DomainResource;

@RestController
@RequestMapping("/api/v1/domains/{domain}/accounts")
public class AccountController {
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private DomainService domainService;
	
	@RequestMapping(method = RequestMethod.GET)
	public List<AccountResource> getAccounts(@PathVariable("domain") String domain) {
		return accountService.getAccountsByDomainName(domain);
	}

	@RequestMapping(method = RequestMethod.POST)
	public void addAccount(@PathVariable("domain") String domainName, @Validated @RequestBody AccountChangeRequest account, BindingResult result) {
		String error = result.hasErrors() ? result.getFieldError().toString() : null;
		saveAccount(domainName, account, error);
	}
	
	@RequestMapping(value = "/{account:.+}", method = RequestMethod.GET)
	public AccountResource getAccount(@PathVariable("domain") String domainName, @PathVariable("account") String accountName) {
		return accountService.getAccountByNameAndDomainName(accountName, domainName);
		
	}

	@RequestMapping(value = "/{account:.+}", method = RequestMethod.DELETE)
	public void deleteAccount(@PathVariable("domain") String domainName, @PathVariable("account") String accountName) {
		accountService.delete(accountName, domainName);
	}
	
	@RequestMapping(value = "/{account:.+}", method = RequestMethod.PUT)
	public void edit(@PathVariable("domain") String domainName, @PathVariable("account") String account, 
			@Validated @RequestBody AccountChangeRequest accountRequest, BindingResult result) {
		String error = result.hasErrors() ? result.getFieldError().toString() : null;
		AccountResource existingAccount = accountService.getAccountByNameAndDomainName(account, domainName);
		accountRequest.setId(existingAccount.getId());
		saveAccount(domainName, accountRequest, error);
	}
	
	private void saveAccount(String domainName, AccountChangeRequest account, String error) {
		if (error != null) {
			throw new AccountException(error);
		}
		if (account.getPassword() != null && !account.getPassword().equals(account.getConfirmPassword())) {
			throw new AccountException("passwords don't match");
		}
		DomainResource domain = domainService.getDomainByName(domainName);
		account.setDomainId(domain.getId());
		accountService.save(account);
	}

}
