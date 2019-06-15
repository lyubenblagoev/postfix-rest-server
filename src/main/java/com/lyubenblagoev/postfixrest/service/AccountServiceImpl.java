package com.lyubenblagoev.postfixrest.service;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.Crypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyubenblagoev.postfixrest.FileUtils;
import com.lyubenblagoev.postfixrest.configuration.MailServerConfiguration;
import com.lyubenblagoev.postfixrest.entity.Account;
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
	
	public AccountServiceImpl(AccountRepository accountRepository, DomainRepository domainRepository,
			MailServerConfiguration mailServerConfiguration) {
		this.accountRepository = accountRepository;
		this.domainRepository = domainRepository;
		this.mailServerConfiguration = mailServerConfiguration;
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
		return domainRepository.findById(account.getDomainId())
				.map(domain -> {
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
					if (account.getPassword() != null && account.getConfirmPassword() != null 
							&& account.getPassword().equals(account.getConfirmPassword())) {
						entity.setPassword(Crypt.crypt(account.getPassword()));
					}
					if (account.getEnabled() != null) {
						entity.setEnabled(account.getEnabled());
					}
					entity.setUpdated(new Date());

					Account saved = accountRepository.save(entity);
					
					return Optional.of(AccountResource.fromAccount(saved));
				})
				.orElse(Optional.empty());
	}
	
	@Override
	@Transactional
	public void delete(AccountResource account) {
		Optional<Domain> existingDomain = domainRepository.findById(account.getDomainId());
		if (existingDomain.isPresent()) {
			accountRepository.delete(AccountResource.toAccount(account, existingDomain.get()));
		}
	}

}
