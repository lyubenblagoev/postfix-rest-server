package com.lyubenblagoev.postfixrest.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyubenblagoev.postfixrest.entity.Alias;
import com.lyubenblagoev.postfixrest.entity.Domain;
import com.lyubenblagoev.postfixrest.repository.AliasRepository;
import com.lyubenblagoev.postfixrest.repository.DomainRepository;
import com.lyubenblagoev.postfixrest.service.model.AliasChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AliasResource;

@Service
@Transactional(readOnly = true)
public class AliasServiceImpl implements AliasService {
	
	@Autowired
	private AliasRepository repository;
	
	@Autowired 
	private DomainRepository domainRepository;
	
	@Override
	public AliasResource getAlias(Long id) {
		Alias entity = repository.findOne(id);
		if (entity == null) {
			throw new AliasNotFoundException("alias with id " + id + " not found");
		}
		return new AliasResource(entity.getId(), entity.getAlias(), entity.getEmail(), entity.isEnabled(), entity.getCreated(), entity.getUpdated());
	}

	@Override
	public Collection<AliasResource> getAliasesByDomainName(String domainName) {
		Collection<Alias> entities = repository.findByDomainName(domainName);
		if (!entities.isEmpty()) {
			return entities.stream().map(e -> {
				return new AliasResource(e.getId(), e.getAlias(), e.getEmail(), e.isEnabled(), e.getCreated(), e.getUpdated());
			}).collect(Collectors.toList());
		}
		return new ArrayList<>(0);
	}
	
	@Override
	public Collection<AliasResource> getAliasByDomainNameAndName(String domainName, String name) {
		Collection<Alias> entities = repository.findByDomainNameAndAlias(domainName, name);
		if (entities.isEmpty()) {
			throw new AliasNotFoundException("alias " + name + " doesn't exist for domain " + domainName);
		}
		return entities.stream().map(e -> {
			return new AliasResource(e.getId(), e.getAlias(), e.getEmail(), e.isEnabled(), e.getCreated(), e.getUpdated());
		}).collect(Collectors.toList());
	}
	
	@Override
	public AliasResource getAliasByDomainNameAndNameAndEmail(String domainName, String name, String email) {
		Alias entity = repository.findByDomainNameAndAliasAndEmail(domainName, name, email);
		if (entity == null) {
			throw new AliasNotFoundException("Alias " + name + " and email recipient " + email + " doesn't exist for domain " + domainName);
		}
		return new AliasResource(entity.getId(), entity.getAlias(), entity.getEmail(), entity.isEnabled(), entity.getCreated(), entity.getUpdated());
	}

	@Override
	@Transactional
	public AliasResource save(AliasChangeRequest alias) {
		Alias entity = alias.getId() != null ? repository.findOne(alias.getId()) : new Alias();
		Domain domain = domainRepository.findOne(alias.getDomainId());
		if (domain == null) {
			throw new DomainNotFoundException("domain with id " + alias.getDomainId() + " not found");
		}
		entity.setDomain(domain);
		if (alias.getId() == null && repository.findByDomainNameAndAliasAndEmail(domain.getName(), alias.getName(), alias.getEmail()) != null) {
			throw new AliasExistsException("alias exists");
		}
		entity.setAlias(alias.getName());
		entity.setEmail(alias.getEmail());
		entity.setUpdated(new Date());
		if (alias.getEnabled() != null) {
			entity.setEnabled(alias.getEnabled());
		}
		entity = repository.save(entity);
		return new AliasResource(entity.getId(), entity.getAlias(), entity.getEmail(), entity.isEnabled(), entity.getCreated(), entity.getUpdated());
	}
	
	@Override
	@Transactional
	public void delete(String domain, String name, String email) {
		Alias existingAlias = repository.findByDomainNameAndAliasAndEmail(domain, name, email);
		if (existingAlias == null) {
			throw new AliasNotFoundException("alias " + name + " with email recipient " + email + " doesn't exist for domain " + domain);
		}
		repository.delete(existingAlias);
	}
	
	@Override
	@Transactional
	public void delete(String domain, String name) {
		Collection<Alias> existingAliases = repository.findByDomainNameAndAlias(domain, name);
		if (existingAliases.isEmpty()) {
			throw new AliasNotFoundException("alias " + name + " doesn't exist for domain " + domain);
		}
		existingAliases.forEach(repository::delete);
	}

}
