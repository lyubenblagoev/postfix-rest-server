package com.lyubenblagoev.postfixrest.service;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.Crypt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyubenblagoev.postfixrest.FileUtils;
import com.lyubenblagoev.postfixrest.configuration.MailServerConfiguration;
import com.lyubenblagoev.postfixrest.entity.Account;
import com.lyubenblagoev.postfixrest.entity.AuditEntityType;
import com.lyubenblagoev.postfixrest.entity.Domain;
import com.lyubenblagoev.postfixrest.repository.AccountRepository;
import com.lyubenblagoev.postfixrest.repository.DomainRepository;
import com.lyubenblagoev.postfixrest.service.model.AccountChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.AccountResource;

@Service
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {
	
	private final AccountRepository accountRepository;
	private final DomainRepository domainRepository;
	private final MailServerConfiguration mailServerConfiguration;
	private final AuditLogService auditLogService;
	
	public AccountServiceImpl(AccountRepository accountRepository, DomainRepository domainRepository,
	                          MailServerConfiguration mailServerConfiguration, AuditLogService auditLogService) {
		this.accountRepository = accountRepository;
		this.domainRepository = domainRepository;
		this.mailServerConfiguration = mailServerConfiguration;
		this.auditLogService = auditLogService;
	}

	@Override
	public Optional<AccountResource> getAccountById(Long id) {
		return accountRepository.findById(id)
				.map(account -> Optional.of(AccountResource.fromAccount(account)))
				.orElse(Optional.empty());
	}
	
	@Override
	public List<AccountResource> getAccountsByDomainName(String name) {
		List<Account> existingAccounts = accountRepository.findByDomainName(name);
		return existingAccounts.stream()
				.map(AccountResource::fromAccount)
				.collect(Collectors.toList());
	} 

	@Override
	public Page<AccountResource> getAccountsByDomainName(String name, Pageable pageable) {
		return accountRepository.findByDomainNameOrderByUsernameAscIdAsc(name, pageable)
				.map(AccountResource::fromAccount);
	}

	@Override
	public Optional<AccountResource> getAccountByNameAndDomainName(String username, String domainName) {
		return accountRepository.findByUsernameAndDomainName(username, domainName)
				.map(account -> Optional.of(AccountResource.fromAccount(account)))
				.orElse(Optional.empty());
	}

	@Override
	@Transactional
	public Optional<AccountResource> save(AccountChangeRequest account) {
		Long id = Optional.ofNullable(account.getId()).orElse(-1L);
		Account entity = accountRepository.findById(id).orElse(new Account());
		boolean newEntity = entity.getId() == null;
		Map<String, ?> oldValues = accountValues(entity);
		return domainRepository.findById(account.getDomainId())
				.map(domain -> {
					Set<String> changedFields = new HashSet<>();
					Optional<Account> existingAccount = accountRepository.findByUsernameAndDomainName(account.getUsername(), domain.getName());
					if (account.getId() == null && existingAccount.isPresent()) {
						throw new EntityExistsException("another account with that name already exists");
					}
					if (account.getId() != null && !account.getUsername().equals(entity.getUsername())) { 
						File domainFolder = new File(mailServerConfiguration.getVhostsPath(), entity.getDomain().getName());
						FileUtils.renameFolder(domainFolder, entity.getUsername(), account.getUsername());
					}

					entity.setDomain(domain);
					entity.setUsername(account.getUsername());
					if (account.getPassword() != null && account.getPasswordConfirmation() != null
							&& account.getPassword().equals(account.getPasswordConfirmation())) {
						entity.setPassword(Crypt.crypt(account.getPassword()));
						changedFields.add("password");
					}
					if (account.getEnabled() != null) {
						entity.setEnabled(account.getEnabled());
					}
					entity.setUpdated(new Date());

					Account saved = accountRepository.save(entity);
					Map<String, ?> newValues = accountValues(saved);
					if (newEntity) {
						auditLogService.recordCreated(AuditEntityType.ACCOUNT, saved.getId(), newValues);
					} else {
						auditLogService.recordUpdated(AuditEntityType.ACCOUNT, saved.getId(), oldValues, newValues,
								changedFields);
					}
					
					return Optional.of(AccountResource.fromAccount(saved));
				})
				.orElse(Optional.empty());
	}
	
	@Override
	@Transactional
	public void delete(AccountResource account) {
		Optional<Domain> existingDomain = domainRepository.findById(account.getDomainId());
		if (existingDomain.isPresent() && account.getId() != null) {
			Optional<Account> existingAccount = accountRepository.findById(account.getId());
			if (existingAccount.isPresent()) {
				Account entity = existingAccount.get();
				accountRepository.delete(entity);
				auditLogService.recordDeleted(AuditEntityType.ACCOUNT, entity.getId(), accountValues(entity));
			}
		}
	}

	private Map<String, ?> accountValues(Account account) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("username", account.getUsername());
		values.put("domainId", account.getDomain() == null ? null : account.getDomain().getId());
		values.put("domainName", account.getDomain() == null ? null : account.getDomain().getName());
		values.put("enabled", account.isEnabled());
		values.put("password", account.getPassword() == null ? null : AuditLogService.REDACTED_VALUE);
		return values;
	}

}
