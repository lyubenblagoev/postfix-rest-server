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

import com.lyubenblagoev.postfixrest.service.AliasNotFoundException;
import com.lyubenblagoev.postfixrest.service.AliasService;
import com.lyubenblagoev.postfixrest.service.BadRequestException;
import com.lyubenblagoev.postfixrest.service.DomainService;
import com.lyubenblagoev.postfixrest.service.model.AliasChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AliasResource;
import com.lyubenblagoev.postfixrest.service.model.DomainResource;

@RestController
@RequestMapping("/api/v1/domains/{domain}/aliases")
public class AliasController {

	@Autowired
	private AliasService service;
	
	@Autowired
	private DomainService domainService;

	@RequestMapping(method = RequestMethod.GET)
	public List<AliasResource> getAliases(@PathVariable("domain") String domain) {
		return service.getAliasesByDomainName(domain);
	}
	
	@RequestMapping(value = "/{alias}", method = RequestMethod.GET)
	public AliasResource getAlias(@PathVariable("domain") String domain, @PathVariable("alias") String alias) {
		return service.getAliasByDomainNameAndAlias(domain, alias);
	}

	@RequestMapping(value = "/{alias}", method = RequestMethod.PUT)
	public void edit(@PathVariable("domain") String domain, @PathVariable("alias") String alias, 
			@Validated @RequestBody AliasChangeRequest aliasRequest, BindingResult result) {
		AliasResource existingAlias = service.getAliasByDomainNameAndAlias(domain, alias);
		if (existingAlias == null) {
			throw new AliasNotFoundException("alias " + alias + " for domain " + domain + " doesn't exist");
		}
		aliasRequest.setId(existingAlias.getId());
		saveAlias(domain, aliasRequest, result);
	}

	@RequestMapping(value = "/{alias}", method = RequestMethod.DELETE)
	public void delete(@PathVariable("domain") String domain, @PathVariable("alias") String alias) {
		service.delete(domain, alias);
	}

	@RequestMapping(method = RequestMethod.POST)
	public void addAlias(@PathVariable("domain") String domain, @Validated @RequestBody AliasChangeRequest alias, BindingResult result) {
		saveAlias(domain, alias, result);
	}

	private void saveAlias(String domain, AliasChangeRequest alias, BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(ControllerUtils.getError(result));
		}
		DomainResource domainEntity = domainService.getDomainByName(domain);
		alias.setDomainId(domainEntity.getId());
		service.save(alias);
	}

}
