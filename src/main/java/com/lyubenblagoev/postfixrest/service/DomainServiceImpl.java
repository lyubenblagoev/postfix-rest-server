package com.lyubenblagoev.postfixrest.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyubenblagoev.postfixrest.entity.Domain;
import com.lyubenblagoev.postfixrest.repository.DomainRepository;
import com.lyubenblagoev.postfixrest.service.model.DomainResource;

@Service
@Transactional(readOnly = true)
public class DomainServiceImpl implements DomainService {
	
	@Autowired
	private DomainRepository repository;
	
	@Override
	public List<DomainResource> getAllDomains() {
		Iterable<Domain> entities = repository.findAll();
		List<DomainResource> domains = new ArrayList<>();
		entities.forEach(e -> domains.add(new DomainResource(e.getId(), e.getName(), e.isEnabled(), e.getCreated(), e.getUpdated())));
		return domains;
	}

	@Override
	public DomainResource getDomain(Long id) {
		Domain entity = repository.findOne(id);
		if (entity == null) {
			throw new DomainNotFoundException("no domain with id " + id);
		}
		return new DomainResource(entity.getId(), entity.getName(), entity.isEnabled(), entity.getCreated(), entity.getUpdated());
	}

	@Override
	public DomainResource getDomainByName(String name) {
		Domain entity = repository.findByName(name);
		if (entity == null) {
			throw new DomainNotFoundException("no domain with name " + name);
		}
		return new DomainResource(entity.getId(), entity.getName(), entity.isEnabled(), entity.getCreated(), entity.getUpdated());
	}

	@Override
	@Transactional
	public DomainResource save(DomainResource domain) {
		if (domain.getId() == null && repository.findByName(domain.getName()) != null) {
			throw new DomainExistsException("domain with that name already exist: " + domain.getName());
		}

		Domain entity = domain.getId() != null ? repository.findOne(domain.getId()) : new Domain();
		entity.setName(domain.getName());
		entity = repository.save(entity);
		return new DomainResource(entity.getId(), entity.getName(), entity.isEnabled(), entity.getCreated(), entity.getUpdated());
	}

	@Override
	@Transactional
	public void delete(Long id) {
		if (!repository.exists(id)) {
			throw new DomainNotFoundException("no domain with id " + id);
		}
		repository.delete(id);
	}

}
