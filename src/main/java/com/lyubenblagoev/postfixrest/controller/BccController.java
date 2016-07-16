package com.lyubenblagoev.postfixrest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lyubenblagoev.postfixrest.service.BadRequestException;
import com.lyubenblagoev.postfixrest.service.BccService;
import com.lyubenblagoev.postfixrest.service.model.BccResource;

@RestController
@RequestMapping("/api/v1/domains/{domainId}/accounts/{accountId}/bccs")
public class BccController {

	@Autowired
	private BccService service;

	@RequestMapping(value = "/outgoing", method = RequestMethod.GET)
	public BccResource getOutgoingBcc(@PathVariable("accountId") Long accountId) {
		return service.getOutgoingBcc(accountId);
	}

	@RequestMapping(value = "/incomming", method = RequestMethod.GET)
	public BccResource getIncommingBcc(@PathVariable("accountId") Long accountId) {
		return service.getIncommingBcc(accountId);
	}

	@RequestMapping(value = "/outgoing", method = RequestMethod.POST)
	public void addOutgoingBcc(@PathVariable("accountId") Long accountId, @Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);
		bcc.setAccountId(accountId);
		service.saveOutgoingBcc(bcc);
	}

	@RequestMapping(value = "/incomming", method = RequestMethod.POST)
	public void addIncommingBcc(@PathVariable("accountId") Long accountId, @Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);
		bcc.setAccountId(accountId);
		service.saveIncommingBcc(bcc);
	}

	@RequestMapping(value = "/outgoing/{id}", method = RequestMethod.PUT)
	public void editOutgoingBcc(@PathVariable("accountId") Long accountId, @PathVariable("id") Long id,
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);
		bcc.setAccountId(accountId);
		bcc.setId(id);
		service.saveOutgoingBcc(bcc);
	}

	@RequestMapping(value = "/incomming/{id}", method = RequestMethod.PUT)
	public void editIncommingBcc(@PathVariable("accountId") Long accountId, @PathVariable("id") Long id,
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);
		bcc.setAccountId(accountId);
		bcc.setId(id);
		service.saveIncommingBcc(bcc);
	}

	@RequestMapping(value = "/outgoing/{id}")
	public void changeOutgoingBcc(@PathVariable("accountId") Long accountId, @PathVariable("id") Long id,
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);
		bcc.setId(id);
		bcc.setAccountId(accountId);
		service.saveOutgoingBcc(bcc);
	}

	@RequestMapping(value = "/incomming/{id}")
	public void changeIncommingBcc(@PathVariable("accountId") Long accountId, @PathVariable("id") Long id,
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);
		bcc.setId(id);
		bcc.setAccountId(accountId);
		service.saveOutgoingBcc(bcc);
	}

	@RequestMapping(value = "/outgoing/{id}", method = RequestMethod.DELETE)
	public void deleteOutgoingBcc(@PathVariable("id") Long id) {
		service.deleteOutgoingBcc(id);
	}

	@RequestMapping(value = "/incomming/{id}", method = RequestMethod.DELETE)
	public void deleteIncommingBcc(@PathVariable("id") Long id) {
		service.deleteIncommingBcc(id);
	}

	private void checkForErrors(BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(result.getFieldError().toString());
		}
	}

}