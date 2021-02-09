package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.BadRequestException;
import com.lyubenblagoev.postfixrest.NotFoundException;
import com.lyubenblagoev.postfixrest.service.AliasService;
import com.lyubenblagoev.postfixrest.service.DomainService;
import com.lyubenblagoev.postfixrest.service.model.AliasChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AliasResource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/domains/{domain}/aliases")
public class AliasController {

	private static final String ALIAS_NOT_FOUND_MESSAGE = "Alias not found";
	
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
	public ResponseEntity<AliasResource> getAlias(@PathVariable("domain") String domain,
			@PathVariable("name") String name, @PathVariable("email") String email) {
		return aliasService.getAliasByDomainNameAndNameAndEmail(domain, name, email)
				.map(alias -> ResponseEntity.ok().body(alias))
				.orElseThrow(() -> new NotFoundException(ALIAS_NOT_FOUND_MESSAGE));
	}

	@PutMapping(value = "/{name}/{email:.+}")
	public ResponseEntity<AliasResource> edit(@PathVariable("domain") String domain,
											  @PathVariable("name") String name, @PathVariable("email") String email,
											  @Valid @RequestBody AliasChangeRequest aliasRequest, BindingResult result) {
		Optional<AliasResource> existingAlias = aliasService.getAliasByDomainNameAndNameAndEmail(domain, name, email);
		return existingAlias.map(a -> {
			aliasRequest.setId(a.getId());
			return saveAlias(domain, aliasRequest, result);
		}).orElseThrow(() -> new NotFoundException(ALIAS_NOT_FOUND_MESSAGE));
	}

	@DeleteMapping(value = "/{name}")
	public void delete(@PathVariable("domain") String domain, @PathVariable("name") String name) {
		aliasService.delete(domain, name);
	}
	
	@DeleteMapping(value = "/{name}/{email:.+}")
	public void delete(@PathVariable("domain") String domain, @PathVariable("name") String name, @PathVariable("email") String email) {
		aliasService.delete(domain, name, email);
	}

	@PostMapping
	public void addAlias(@PathVariable("domain") String domain, @Valid @RequestBody AliasChangeRequest alias, BindingResult result) {
		saveAlias(domain, alias, result);
	}

	private ResponseEntity<AliasResource> saveAlias(String domain, AliasChangeRequest alias, BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(ControllerUtils.getError(result));
		}
		return domainService.getDomainByName(domain)
				.map(d -> {
					alias.setDomainId(d.getId());
					return aliasService.save(alias)
							.map(saved -> ResponseEntity.ok().body(saved))
							.orElseThrow(BadRequestException::new);
				})
				.orElseThrow(() -> new NotFoundException("Domain " + domain + " not found"));
	}

}
