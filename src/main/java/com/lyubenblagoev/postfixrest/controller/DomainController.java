package com.lyubenblagoev.postfixrest.controller;

import java.util.List;

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
	
	@GetMapping
	public List<DomainResource> listDomains() {
		return domainService.getAllDomains();
	}
	
	@PostMapping
	public DomainResource addDomain(@RequestBody DomainResource domain) {
		return domainService.save(domain);
	}

	@GetMapping(value = "/{name:.+}")
	public DomainResource getDomain(@PathVariable("name") String name) {
		return domainService.getDomainByName(name);
	}
	
	@DeleteMapping(value = "/{name:.+}")
	public void delete(@PathVariable("name") String name) {
		domainService.delete(name);
	}
	
	@PutMapping(value = "/{name:.+}")
	public void edit(@PathVariable("name") String name, @Validated @RequestBody DomainResource domain, BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(ControllerUtils.getError(result));
		}
		DomainResource existingDomain = domainService.getDomainByName(name);
		domain.setId(existingDomain.getId());
		domainService.save(domain);
	}

}
