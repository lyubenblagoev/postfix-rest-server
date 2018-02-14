package com.lyubenblagoev.postfixrest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lyubenblagoev.postfixrest.service.AccountService;
import com.lyubenblagoev.postfixrest.service.BadRequestException;
import com.lyubenblagoev.postfixrest.service.BccService;
import com.lyubenblagoev.postfixrest.service.model.AccountResource;
import com.lyubenblagoev.postfixrest.service.model.BccResource;

@RestController
@RequestMapping("/api/v1/domains/{domain}/accounts/{account}/bccs")
public class BccController {

	@Autowired
	private BccService service;
	
	@Autowired
	private AccountService accountService;
	
	@RequestMapping(value = "/outgoing", method = RequestMethod.GET)
	public BccResource getOutgoingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account) {
		return service.getOutgoingBcc(domain, account);
	}

	@RequestMapping(value = "/incomming", method = RequestMethod.GET)
	public BccResource getIncommingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account) {
		return service.getIncommingBcc(domain, account);
	}

	@RequestMapping(value = "/outgoing", method = RequestMethod.POST)
	public void addOutgoingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account, 
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);

		AccountResource accountResource = accountService.getAccountByNameAndDomainName(account, domain);
		bcc.setAccountId(accountResource.getId());

		service.saveOutgoingBcc(bcc);
	}

	@RequestMapping(value = "/incomming", method = RequestMethod.POST)
	public void addIncommingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account, 
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);

		AccountResource accountResource = accountService.getAccountByNameAndDomainName(account, domain);
		bcc.setAccountId(accountResource.getId());

		service.saveIncommingBcc(bcc);
	}

	@RequestMapping(value = "/outgoing", method = RequestMethod.PUT)
	public void editOutgoingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account, 
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);

		AccountResource accountResource = accountService.getAccountByNameAndDomainName(account, domain);
		bcc.setAccountId(accountResource.getId());

		BccResource existingBcc = service.getOutgoingBcc(domain, account);
		bcc.setId(existingBcc.getId());
		if (bcc.getEmail() == null) {
			bcc.setEmail(existingBcc.getEmail());
		}

		service.saveOutgoingBcc(bcc);
	}

	@RequestMapping(value = "/incomming", method = RequestMethod.PUT)
	public void editIncommingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account, 
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);

		AccountResource accountResource = accountService.getAccountByNameAndDomainName(account, domain);
		bcc.setAccountId(accountResource.getId());

		BccResource existingBcc = service.getIncommingBcc(domain, account);
		bcc.setId(existingBcc.getId());
		if (bcc.getEmail() == null) {
			bcc.setEmail(existingBcc.getEmail());
		}

		service.saveIncommingBcc(bcc);
	}

	@RequestMapping(value = "/outgoing")
	public void changeOutgoingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account, 
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);

		AccountResource accountResource = accountService.getAccountByNameAndDomainName(account, domain);
		bcc.setAccountId(accountResource.getId());

		BccResource existingBcc = service.getOutgoingBcc(domain, account);
		bcc.setId(existingBcc.getId());

		service.saveOutgoingBcc(bcc);
	}

	@RequestMapping(value = "/incomming")
	public void changeIncommingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account, 
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);

		AccountResource accountResource = accountService.getAccountByNameAndDomainName(account, domain);
		bcc.setAccountId(accountResource.getId());

		BccResource existingBcc = service.getIncommingBcc(domain, account);
		bcc.setId(existingBcc.getId());

		service.saveOutgoingBcc(bcc);
	}

	@RequestMapping(value = "/outgoing", method = RequestMethod.DELETE)
	public void deleteOutgoingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account) {
		service.deleteOutgoingBcc(domain, account);
	}

	@RequestMapping(value = "/incomming", method = RequestMethod.DELETE)
	public void deleteIncommingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account) {
		service.deleteIncommingBcc(domain, account);
	}

	private void checkForErrors(BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(ControllerUtils.getError(result));
		}
	}

}