package com.lyubenblagoev.postfixrest.controller;

import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lyubenblagoev.postfixrest.service.BadRequestException;
import com.lyubenblagoev.postfixrest.service.DomainService;
import com.lyubenblagoev.postfixrest.service.model.DomainResource;

@RestController
@RequestMapping("/api/v1/domains")
public class DomainController {
	
	private final DomainService domainService;
	
	public DomainController(DomainService domainService) {
		this.domainService = domainService;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public List<DomainResource> listDomains() {
		return domainService.getAllDomains();
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public DomainResource addDomain(@RequestBody DomainResource domain) {
		return domainService.save(domain);
	}

	@RequestMapping(value = "/{name:.+}", method = RequestMethod.GET)
	public DomainResource getDomain(@PathVariable("name") String name) {
		return domainService.getDomainByName(name);
	}
	
	@RequestMapping(value = "/{name:.+}", method = RequestMethod.DELETE)
	public void delete(@PathVariable("name") String name) {
		domainService.delete(name);
	}
	
	@RequestMapping(value = "/{name:.+}", method = RequestMethod.PUT)
	public void edit(@PathVariable("name") String name, @Validated @RequestBody DomainResource domain, BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(ControllerUtils.getError(result));
		}
		DomainResource existingDomain = domainService.getDomainByName(name);
		domain.setId(existingDomain.getId());
		domainService.save(domain);
	}

}
