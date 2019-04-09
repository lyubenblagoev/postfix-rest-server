package com.lyubenblagoev.postfixrest.controller;

import java.util.Collection;

import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

	private final AliasService aliasService;
	private final DomainService domainService;
	
	public AliasController(AliasService aliasService, DomainService domainService) {
		this.aliasService = aliasService;
		this.domainService = domainService;
	}

	@GetMapping
	public Collection<AliasResource> getAliases(@PathVariable("domain") String domain) {
		return aliasService.getAliasesByDomainName(domain);
	}
	
	@GetMapping(value = "/{name}")
	public Collection<AliasResource> getAliases(@PathVariable("domain") String domain, @PathVariable("name") String name) {
		return aliasService.getAliasByDomainNameAndName(domain, name);
	}
	
	@GetMapping(value = "/{name}/{email:.+}")
	public AliasResource getAlias(@PathVariable("domain") String domain, @PathVariable("name") String name, @PathVariable("email") String email) {
		return aliasService.getAliasByDomainNameAndNameAndEmail(domain, name, email);
	}

	@PutMapping(value = "/{name}/{email:.+}")
	public void edit(@PathVariable("domain") String domain, @PathVariable("name") String name, @PathVariable("email") String email, 
			@Validated @RequestBody AliasChangeRequest aliasRequest, BindingResult result) {
		AliasResource existingAlias = aliasService.getAliasByDomainNameAndNameAndEmail(domain, name, email);
		if (existingAlias == null) {
			throw new AliasNotFoundException("alias " + name + " for domain " + domain + " doesn't exist");
		}
		aliasRequest.setId(existingAlias.getId());
		saveAlias(domain, aliasRequest, result);
	}

	@DeleteMapping(value = "/{name}")
	public void delete(@PathVariable("domain") String domain, @PathVariable("name") String name) {
		aliasService.delete(domain, name);
	}
	
	@DeleteMapping(value = "/{name}/{email:.+}")
	public void delete(@PathVariable("domain") String domain, @PathVariable("name") String name, @PathVariable("email") String email) {
		aliasService.delete(domain, name, email);
	}

	@PutMapping
	public void addAlias(@PathVariable("domain") String domain, @Validated @RequestBody AliasChangeRequest alias, BindingResult result) {
		saveAlias(domain, alias, result);
	}

	private void saveAlias(String domain, AliasChangeRequest alias, BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(ControllerUtils.getError(result));
		}
		DomainResource domainEntity = domainService.getDomainByName(domain);
		alias.setDomainId(domainEntity.getId());
		aliasService.save(alias);
	}

}
