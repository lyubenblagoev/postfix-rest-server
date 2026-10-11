package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.BadRequestException;
import com.lyubenblagoev.postfixrest.NotFoundException;
import com.lyubenblagoev.postfixrest.configuration.PaginationProperties;
import com.lyubenblagoev.postfixrest.service.AccountService;
import com.lyubenblagoev.postfixrest.service.DomainService;
import com.lyubenblagoev.postfixrest.service.model.AccountChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AccountResource;
import com.lyubenblagoev.postfixrest.service.model.PagedResponse;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/domains/{domain}/accounts")
public class AccountController {

	private static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account not found";

	private final AccountService accountService;
	private final DomainService domainService;
	private final PaginationProperties paginationProperties;

	public AccountController(AccountService accountService, DomainService domainService,
	                         PaginationProperties paginationProperties) {
		this.accountService = accountService;
		this.domainService = domainService;
		this.paginationProperties = paginationProperties;
	}

	@GetMapping
	public PagedResponse<AccountResource> getAccounts(@PathVariable("domain") String domain,
	                                                  @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer pageSize) {
		return PagedResponse.from(accountService.getAccountsByDomainName(domain,
				paginationProperties.toPageable(page, pageSize, Sort.by("username", "id"))));
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
