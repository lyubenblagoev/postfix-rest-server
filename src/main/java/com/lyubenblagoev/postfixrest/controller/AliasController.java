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

import com.lyubenblagoev.postfixrest.service.AliasService;
import com.lyubenblagoev.postfixrest.service.BadRequestException;
import com.lyubenblagoev.postfixrest.service.model.AliasChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AliasResource;

@RestController
@RequestMapping("/api/v1/domains/{domainId}/aliases")
public class AliasController {

	@Autowired
	private AliasService service;

	@RequestMapping(method = RequestMethod.GET)
	public List<AliasResource> getAliases(@PathVariable("domainId") Long domainId) {
		return service.getAliasesForDomain(domainId);
	}
	
	@RequestMapping(value = "/{aliasId}", method = RequestMethod.GET)
	public AliasResource getAlias(@PathVariable("domainId") Long domainId, @PathVariable("aliasId") Long aliasId) {
		return service.getAlias(aliasId);
	}

	@RequestMapping(value = "/{aliasId}", method = RequestMethod.PUT)
	public void edit(@PathVariable("domainId") Long domainId, @PathVariable("aliasId") Long aliasId, 
			@Validated @RequestBody AliasChangeRequest alias, BindingResult result) {
		alias.setId(aliasId);
		saveAlias(domainId, alias, result);
	}

	@RequestMapping(value = "/{aliasId}", method = RequestMethod.DELETE)
	public void delete(@PathVariable("domainId") Long domainId, @PathVariable("aliasId") Long aliasId) {
		service.delete(aliasId);
	}

	@RequestMapping(method = RequestMethod.POST)
	public void addAlias(@PathVariable("domainId") Long domainId, @Validated @RequestBody AliasChangeRequest alias, BindingResult result) {
		saveAlias(domainId, alias, result);
	}

	private void saveAlias(Long domainId, AliasChangeRequest alias, BindingResult result) {
		String error = result.hasErrors() ? result.getFieldError().toString() : null;
		if (error != null) {
			throw new BadRequestException(error);
		}
		alias.setDomainId(domainId);
		service.save(alias);
	}

}
