package com.lyubenblagoev.postfixrest.controller;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.lyubenblagoev.postfixrest.BadRequestException;
import com.lyubenblagoev.postfixrest.NotFoundException;
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
	public ResponseEntity<DomainResource> addDomain(@Validated @RequestBody DomainResource domain, BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(ControllerUtils.getError(result));
		}
		return domainService.save(domain)
				.map(d -> ResponseEntity.status(HttpStatus.CREATED).body(domain))
				.orElseThrow(() -> new NotFoundException("Failed to save domain " + domain.getName()));
	}

	@GetMapping(value = "/{name:.+}")
	public ResponseEntity<DomainResource> getDomain(@PathVariable("name") String name) {
		return domainService.getDomainByName(name)
				.map(domain -> ResponseEntity.ok().body(domain))
				.orElseThrow(() -> new NotFoundException("Domain " + name + " not found"));
	}
	
	@DeleteMapping(value = "/{name:.+}")
	public ResponseEntity<?> delete(@PathVariable("name") String name, HttpServletRequest request) {
		Optional<DomainResource> domain = domainService.getDomainByName(name);
		return domain.map(d -> {
			domainService.delete(d);
			return ResponseEntity.ok().build();
		}).orElseThrow(() -> new NotFoundException("Domain " + name + " not found"));
	}
	
	@PutMapping(value = "/{name:.+}")
	public ResponseEntity<DomainResource> edit(@PathVariable("name") String name,
			@Validated @RequestBody DomainResource domain, BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(ControllerUtils.getError(result));
		}
		
		Optional<DomainResource> existingDomain = domainService.getDomainByName(name);
		return existingDomain.map(d -> {
			domain.setId(d.getId());
			return domainService.save(domain)
					.map(saved -> ResponseEntity.status(HttpStatus.OK).body(saved))
					.orElseThrow(BadRequestException::new);
		}).orElseThrow(() -> new NotFoundException("Domain " + name + " not found"));
	}

}
