package com.lyubenblagoev.postfixrest.service;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyubenblagoev.postfixrest.NotFoundException;
import com.lyubenblagoev.postfixrest.entity.Alias;
import com.lyubenblagoev.postfixrest.repository.AliasRepository;
import com.lyubenblagoev.postfixrest.repository.DomainRepository;
import com.lyubenblagoev.postfixrest.service.model.AliasChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AliasResource;

@Service
@Transactional(readOnly = true)
public class AliasServiceImpl implements AliasService {
	
	private final AliasRepository aliasRepository;
	private final DomainRepository domainRepository;
	
	public AliasServiceImpl(AliasRepository aliasRepository, DomainRepository domainRepository) {
		this.aliasRepository = aliasRepository;
		this.domainRepository = domainRepository;
	}
	
	@Override
	public Optional<AliasResource> getAlias(Long id) {
		return aliasRepository.findById(id)
				.map(alias -> Optional.of(AliasResource.fromAlias(alias)))
				.orElse(Optional.empty());
	}

	@Override
	public Collection<AliasResource> getAliasesByDomainName(String domainName) {
		return aliasRepository.findByDomainName(domainName).stream()
				.map(AliasResource::fromAlias)
				.collect(Collectors.toList());
	}
	
	@Override
	public Collection<AliasResource> getAliasByDomainNameAndName(String domainName, String name) {
		return aliasRepository.findByDomainNameAndAlias(domainName, name).stream()
				.map(AliasResource::fromAlias)
				.collect(Collectors.toList());
	}
	
	@Override
	public Optional<AliasResource> getAliasByDomainNameAndNameAndEmail(String domainName, String name, String email) {
		return aliasRepository.findByDomainNameAndAliasAndEmail(domainName, name, email)
				.map(alias -> Optional.of(AliasResource.fromAlias(alias)))
				.orElse(Optional.empty());
	}

	@Override
	@Transactional
	public Optional<AliasResource> save(AliasChangeRequest alias) {
		Long id = Optional.ofNullable(alias.getId()).orElse(-1L);
		Alias existingAlias = aliasRepository.findById(id).orElse(new Alias());
		return domainRepository.findById(alias.getDomainId())
				.map(domain -> {
					Optional<Alias> sameAlias = aliasRepository.findByDomainNameAndAliasAndEmail(
							domain.getName(), alias.getName(), alias.getEmail());
					if (alias.getId() == null && sameAlias.isPresent()) {
						throw new EntityExistsException("another alias with the same parameters already exists");
					}
					existingAlias.setDomain(domain);
					existingAlias.setAlias(alias.getName());
					existingAlias.setEmail(alias.getEmail());
					existingAlias.setUpdated(new Date());
					if (alias.getEnabled() != null) {
						existingAlias.setEnabled(alias.getEnabled());
					}
					Alias saved = aliasRepository.save(existingAlias);
					return Optional.of(AliasResource.fromAlias(saved));
				})
				.orElse(Optional.empty());
	}
	
	@Override
	@Transactional
	public void delete(String domain, String name, String email) {
		Optional<Alias> existingAlias = aliasRepository.findByDomainNameAndAliasAndEmail(domain, name, email);
		if (existingAlias.isPresent()) {
			aliasRepository.delete(existingAlias.get());
		} else {
			throw new NotFoundException("Alias not found.");
		}
	}
	
	@Override
	@Transactional
	public void delete(String domain, String name) {
		Collection<Alias> existingAliases = aliasRepository.findByDomainNameAndAlias(domain, name);
		if (existingAliases.isEmpty()) {
			throw new NotFoundException("No aliases exist for the specified parameters");
		}
		existingAliases.forEach(aliasRepository::delete);
	}

}
