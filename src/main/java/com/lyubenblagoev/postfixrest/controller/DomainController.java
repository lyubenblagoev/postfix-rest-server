package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.BadRequestException;
import com.lyubenblagoev.postfixrest.NotFoundException;
import com.lyubenblagoev.postfixrest.security.UserPrincipal;
import com.lyubenblagoev.postfixrest.service.DomainService;
import com.lyubenblagoev.postfixrest.service.model.DomainResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

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
	public ResponseEntity<DomainResource> addDomain(@Valid @RequestBody DomainResource domain, BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(ControllerUtils.getError(result));
		}
		return domainService.save(domain)
				.map(d -> ResponseEntity.status(HttpStatus.CREATED).body(domain))
				.orElseThrow(() -> new NotFoundException("Failed to update domain " + domain.getName()));
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
			@Valid @RequestBody DomainResource domain, BindingResult result) {
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
