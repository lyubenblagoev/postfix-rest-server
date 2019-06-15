package com.lyubenblagoev.postfixrest.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyubenblagoev.postfixrest.FileUtils;
import com.lyubenblagoev.postfixrest.configuration.MailServerConfiguration;
import com.lyubenblagoev.postfixrest.entity.Domain;
import com.lyubenblagoev.postfixrest.repository.DomainRepository;
import com.lyubenblagoev.postfixrest.service.model.DomainResource;

@Service
@Transactional(readOnly = true)
public class DomainServiceImpl implements DomainService {
	
	private final DomainRepository domainRepository;
	private final MailServerConfiguration mailServerConfiguration;
	
	public DomainServiceImpl(DomainRepository domainRepository, MailServerConfiguration mailServerConfiguration) {
		this.domainRepository = domainRepository;
		this.mailServerConfiguration = mailServerConfiguration;
	}
	
	@Override
	public List<DomainResource> getAllDomains() {
		Iterable<Domain> entities = domainRepository.findAll();
		List<DomainResource> domains = new ArrayList<>();
		entities.forEach(e -> domains.add(DomainResource.fromDomain(e)));
		return domains;
	}

	@Override
	public Optional<DomainResource> getDomainByName(String name) {
		return domainRepository.findByName(name)
				.map(domain -> Optional.of(DomainResource.fromDomain(domain)))
				.orElse(Optional.empty());
	}

	@Override
	@Transactional
	public Optional<DomainResource> save(DomainResource domain) {
		Long id = domain.getId() != null ? domain.getId() : -1L;
		String name = domain.getName();

		Domain entity = domainRepository.findById(id).orElseGet(Domain::new);
		
		if (domain.getId() == null) {
			Optional<Domain> domainByName = domainRepository.findByName(name);
			if (domainByName.isPresent()) {
				throw new EntityExistsException("domain with that name already exist: " + name);
			}
		} else if (!entity.getName().equals(domain.getName())) {
			FileUtils.renameFolder(new File(mailServerConfiguration.getVhostsPath()), entity.getName(), domain.getName());
		}

		entity.setName(domain.getName());
		if (domain.getEnabled() != null) {
			entity.setEnabled(domain.getEnabled()); 
		}
		entity.setUpdated(new Date());

		entity = domainRepository.save(entity);
		
		return Optional.of(DomainResource.fromDomain(entity));
	}

	@Override
	@Transactional
	public void delete(DomainResource domain) {
		FileUtils.deleteFolder(new File(mailServerConfiguration.getVhostsPath()), domain.getName());
		domainRepository.delete(DomainResource.toDomain(domain));
	}

}
