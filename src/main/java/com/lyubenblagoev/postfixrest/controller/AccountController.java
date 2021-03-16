package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.BadRequestException;
import com.lyubenblagoev.postfixrest.NotFoundException;
import com.lyubenblagoev.postfixrest.service.AccountService;
import com.lyubenblagoev.postfixrest.service.DomainService;
import com.lyubenblagoev.postfixrest.service.model.AccountChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AccountResource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/domains/{domain}/accounts")
public class AccountController {

	private static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account not found";

	private final AccountService accountService;
	private final DomainService domainService;

	public AccountController(AccountService accountService, DomainService domainService) {
		this.accountService = accountService;
		this.domainService = domainService;
	}

	@GetMapping
	public List<AccountResource> getAccounts(@PathVariable("domain") String domain) {
		return accountService.getAccountsByDomainName(domain);
	}

	@GetMapping(value = "/{account:.+}")
	public ResponseEntity<AccountResource> getAccount(@PathVariable("domain") String domainName, @PathVariable("account") String accountName) {
		return accountService.getAccountByNameAndDomainName(accountName, domainName)
				.map(account -> ResponseEntity.ok().body(account))
				.orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));

	}

	@PostMapping
	public ResponseEntity<?> addAccount(@PathVariable("domain") String domainName,
										@Valid @RequestBody AccountChangeRequest account, BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(ControllerUtils.getError(result));
		}
		return domainService.getDomainByName(domainName).map(domain -> {
			account.setDomainId(domain.getId());
			accountService.save(account);
			return ResponseEntity.ok().build();
		}).orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
	}

	@DeleteMapping(value = "/{account:.+}")
	public ResponseEntity<?> deleteAccount(@PathVariable("domain") String domainName, @PathVariable("account") String accountName) {
		return accountService.getAccountByNameAndDomainName(accountName, domainName)
				.map(account -> {
					accountService.delete(account);
					return ResponseEntity.ok().build();
				})
				.orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
	}

	@PutMapping(value = "/{account:.+}")
	public ResponseEntity<?> edit(@PathVariable("domain") String domainName, @PathVariable("account") String account,
			@Valid @RequestBody AccountChangeRequest accountRequest, BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(ControllerUtils.getError(result));
		}
		return accountService.getAccountByNameAndDomainName(account, domainName)
				.map(a -> {
					accountRequest.setId(a.getId());
					return domainService.getDomainByName(domainName)
							.map(domain -> {
								accountRequest.setDomainId(domain.getId());
								accountService.save(accountRequest);
								return ResponseEntity.ok().build();
							})
							.orElseThrow(() -> new NotFoundException("Domain " + domainName + " not found."));
				})
				.orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
	}

}
