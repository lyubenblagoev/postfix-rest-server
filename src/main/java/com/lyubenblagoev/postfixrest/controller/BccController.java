package com.lyubenblagoev.postfixrest.controller;

import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyubenblagoev.postfixrest.service.AccountService;
import com.lyubenblagoev.postfixrest.service.BadRequestException;
import com.lyubenblagoev.postfixrest.service.BccService;
import com.lyubenblagoev.postfixrest.service.model.AccountResource;
import com.lyubenblagoev.postfixrest.service.model.BccResource;

@RestController
@RequestMapping("/api/v1/domains/{domain}/accounts/{account}/bccs")
public class BccController {

	private final BccService bccService;
	private final AccountService accountService;
	
	public BccController(BccService bccService, AccountService accountService) {
		this.bccService = bccService;
		this.accountService = accountService;
	}
	
	@GetMapping(value = "/outgoing")
	public BccResource getOutgoingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account) {
		return bccService.getOutgoingBcc(domain, account);
	}

	@GetMapping(value = "/incomming")
	public BccResource getIncommingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account) {
		return bccService.getIncommingBcc(domain, account);
	}

	@PostMapping(value = "/outgoing")
	public void addOutgoingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account, 
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);

		AccountResource accountResource = accountService.getAccountByNameAndDomainName(account, domain);
		bcc.setAccountId(accountResource.getId());

		bccService.saveOutgoingBcc(bcc);
	}

	@PostMapping(value = "/incomming")
	public void addIncommingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account, 
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);

		AccountResource accountResource = accountService.getAccountByNameAndDomainName(account, domain);
		bcc.setAccountId(accountResource.getId());

		bccService.saveIncommingBcc(bcc);
	}

	@PutMapping(value = "/outgoing")
	public void editOutgoingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account, 
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);

		AccountResource accountResource = accountService.getAccountByNameAndDomainName(account, domain);
		bcc.setAccountId(accountResource.getId());

		BccResource existingBcc = bccService.getOutgoingBcc(domain, account);
		bcc.setId(existingBcc.getId());
		if (bcc.getEmail() == null) {
			bcc.setEmail(existingBcc.getEmail());
		}

		bccService.saveOutgoingBcc(bcc);
	}

	@PutMapping(value = "/incomming")
	public void editIncommingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account, 
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);

		AccountResource accountResource = accountService.getAccountByNameAndDomainName(account, domain);
		bcc.setAccountId(accountResource.getId());

		BccResource existingBcc = bccService.getIncommingBcc(domain, account);
		bcc.setId(existingBcc.getId());
		if (bcc.getEmail() == null) {
			bcc.setEmail(existingBcc.getEmail());
		}

		bccService.saveIncommingBcc(bcc);
	}

	@DeleteMapping(value = "/outgoing")
	public void deleteOutgoingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account) {
		bccService.deleteOutgoingBcc(domain, account);
	}

	@DeleteMapping(value = "/incomming")
	public void deleteIncommingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account) {
		bccService.deleteIncommingBcc(domain, account);
	}

	private void checkForErrors(BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(ControllerUtils.getError(result));
		}
	}

}