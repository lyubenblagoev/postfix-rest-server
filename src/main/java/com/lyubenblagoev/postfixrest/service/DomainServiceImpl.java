package com.lyubenblagoev.postfixrest.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyubenblagoev.postfixrest.FileUtils;
import com.lyubenblagoev.postfixrest.configuration.MailServerConfiguration;
import com.lyubenblagoev.postfixrest.entity.AuditEntityType;
import com.lyubenblagoev.postfixrest.entity.Account;
import com.lyubenblagoev.postfixrest.entity.Alias;
import com.lyubenblagoev.postfixrest.entity.Domain;
import com.lyubenblagoev.postfixrest.repository.AccountRepository;
import com.lyubenblagoev.postfixrest.repository.AliasRepository;
import com.lyubenblagoev.postfixrest.repository.DomainRepository;
import com.lyubenblagoev.postfixrest.service.model.DomainResource;

@Service
@Transactional(readOnly = true)
public class DomainServiceImpl implements DomainService {
	
	private final DomainRepository domainRepository;
	private final AccountRepository accountRepository;
	private final AliasRepository aliasRepository;
	private final MailServerConfiguration mailServerConfiguration;
	private final AuditLogService auditLogService;
	
	public DomainServiceImpl(DomainRepository domainRepository, AccountRepository accountRepository,
			AliasRepository aliasRepository, MailServerConfiguration mailServerConfiguration,
			AuditLogService auditLogService) {
		this.domainRepository = domainRepository;
		this.accountRepository = accountRepository;
		this.aliasRepository = aliasRepository;
		this.mailServerConfiguration = mailServerConfiguration;
		this.auditLogService = auditLogService;
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
		boolean newEntity = entity.getId() == null;
		Map<String, ?> oldValues = domainValues(entity);
		
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
		Map<String, ?> newValues = domainValues(entity);
		if (newEntity) {
			auditLogService.recordCreated(AuditEntityType.DOMAIN, entity.getId(), newValues);
		} else {
			auditLogService.recordUpdated(AuditEntityType.DOMAIN, entity.getId(), oldValues, newValues);
		}
		
		return Optional.of(DomainResource.fromDomain(entity));
	}

	@Override
	@Transactional
	public void delete(DomainResource domain) {
		List<Account> accounts = accountRepository.findByDomainName(domain.getName());
		Collection<Alias> aliases = aliasRepository.findByDomainId(domain.getId());
		FileUtils.deleteFolder(new File(mailServerConfiguration.getVhostsPath()), domain.getName());
		domainRepository.delete(DomainResource.toDomain(domain));
		accounts.forEach(account -> auditLogService.recordDeleted(AuditEntityType.ACCOUNT, account.getId(),
				accountValues(account)));
		aliases.forEach(alias -> auditLogService.recordDeleted(AuditEntityType.ALIAS, alias.getId(),
				aliasValues(alias)));
		auditLogService.recordDeleted(AuditEntityType.DOMAIN, domain.getId(), domainValues(domain));
	}

	private Map<String, ?> domainValues(Domain domain) {
		return domainValues(domain.getName(), domain.isEnabled());
	}

	private Map<String, ?> domainValues(DomainResource domain) {
		return domainValues(domain.getName(), domain.getEnabled());
	}

	private Map<String, ?> domainValues(String name, Boolean enabled) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("name", name);
		values.put("enabled", enabled);
		return values;
	}

	private Map<String, ?> accountValues(Account account) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("username", account.getUsername());
		values.put("domainId", account.getDomain() == null ? null : account.getDomain().getId());
		values.put("enabled", account.isEnabled());
		values.put("password", account.getPassword() == null ? null : AuditLogService.REDACTED_VALUE);
		return values;
	}

	private Map<String, ?> aliasValues(Alias alias) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("domainId", alias.getDomain() == null ? null : alias.getDomain().getId());
		values.put("name", alias.getAlias());
		values.put("email", alias.getEmail());
		values.put("enabled", alias.isEnabled());
		return values;
	}

}
