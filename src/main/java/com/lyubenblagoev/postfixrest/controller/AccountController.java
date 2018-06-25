package com.lyubenblagoev.postfixrest.controller;

import java.util.List;

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
	
	private final AccountService accountService;
	private final DomainService domainService;
	
	public AccountController(AccountService accountService, DomainService domainService) {
		this.accountService = accountService;
		this.domainService = domainService;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public List<AccountResource> getAccounts(@PathVariable("domain") String domain) {
		return accountService.getAccountsByDomainName(domain);
	}

	@RequestMapping(method = RequestMethod.POST)
	public void addAccount(@PathVariable("domain") String domainName, @Validated @RequestBody AccountChangeRequest account, BindingResult result) {
		saveAccount(domainName, account, ControllerUtils.getError(result));
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
		AccountResource existingAccount = accountService.getAccountByNameAndDomainName(account, domainName);
		accountRequest.setId(existingAccount.getId());
		saveAccount(domainName, accountRequest, ControllerUtils.getError(result));
	}
	
	private void saveAccount(String domainName, AccountChangeRequest account, String error) {
		if (error != null) {
			throw new AccountException(error);
		}
		DomainResource domain = domainService.getDomainByName(domainName);
		account.setDomainId(domain.getId());
		accountService.save(account);
	}

}
