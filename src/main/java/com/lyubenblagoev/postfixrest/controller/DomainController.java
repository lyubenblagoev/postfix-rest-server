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

import com.lyubenblagoev.postfixrest.service.BadRequestException;
import com.lyubenblagoev.postfixrest.service.DomainService;
import com.lyubenblagoev.postfixrest.service.model.DomainResource;

@RestController
@RequestMapping("/api/v1/domains")
public class DomainController {
	
	@Autowired
	private DomainService domainService;
	
	@RequestMapping(method = RequestMethod.GET)
	public List<DomainResource> listDomains() {
		return domainService.getAllDomains();
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public DomainResource addDomain(@RequestBody DomainResource domain) {
		return domainService.save(domain);
	}

	@RequestMapping(value = "/{domainId}", method = RequestMethod.GET)
	public DomainResource getDomain(@PathVariable("domainId") Long domainId) {
		return domainService.getDomain(domainId);
	}
	
	@RequestMapping(value = "/{domainId}", method = RequestMethod.DELETE)
	public void delete(@PathVariable("domainId") Long domainId) {
		domainService.delete(domainId);
	}
	
	@RequestMapping(value = "/{domainId}", method = RequestMethod.PUT)
	public void edit(@PathVariable("domainId") Long domainId, @Validated @RequestBody DomainResource domain, BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(result.getFieldError().getDefaultMessage());
		}
		domain.setId(domainId);
		domainService.save(domain);
	}

}
