package com.lyubenblagoev.postfixrest.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.Crypt;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Autowired
	private AccountRepository repository;
	
	@Autowired
	private DomainRepository domainRepository;
	
	@Autowired
	private MailServerConfiguration mailServerConfiguration;

	@Override
	public AccountResource getAccountById(Long id) {
		Account entity = repository.findOne(id);
		if (entity == null) {
			throw new AccountNotFoundException("no account with id " + id);
		}
		return new AccountResource(entity.getId(), entity.getUsername(), entity.getDomain().getName(), 
				entity.getDomain().getId(), entity.isEnabled(), entity.getCreated(), entity.getUpdated());
	}
	
	@Override
	public List<AccountResource> getAccountsByDomainName(String name) {
		List<Account> entities = repository.findByDomainName(name);
		List<AccountResource> accounts = new ArrayList<>(entities.size());
		entities.forEach(e -> accounts.add(new AccountResource(e.getId(), e.getUsername(), e.getDomain().getName(), 
				e.getDomain().getId(), e.isEnabled(), e.getCreated(), e.getUpdated())));
		return accounts;
	} 

	@Override
	public AccountResource getAccountByNameAndDomainName(String username, String domainName) {
		Account account = repository.findByUsernameAndDomainName(username, domainName);
		if (account == null) {
			throw new AccountNotFoundException("account with username " + username + " and domain " + domainName + " not found");
		}
		return new AccountResource(account.getId(), account.getUsername(), account.getDomain().getName(), 
				account.getDomain().getId(), account.isEnabled(), account.getCreated(), account.getUpdated());
	}

	@Override
	@Transactional
	public AccountResource save(AccountChangeRequest account) {
		Account entity = account.getId() == null ? new Account() : repository.findOne(account.getId());

		Domain domain = domainRepository.findOne(account.getDomainId());
		if (domain == null) {
			throw new DomainNotFoundException("domain with id " + account.getDomainId() + " not found");
		}
		entity.setDomain(domain);

		if (account.getId() == null && repository.findByUsernameAndDomainName(account.getUsername(), domain.getName()) != null) {
			throw new AccountExistsException("another account with that name already exists");
		}
		
		if (account.getId() != null && !account.getUsername().equals(entity.getUsername())) { 
			File domainFolder = new File(mailServerConfiguration.getVhostsPath(), entity.getDomain().getName());
			FileUtils.renameFolder(domainFolder, entity.getUsername(), account.getUsername());
		}

		entity.setUsername(account.getUsername());
		if (account.getPassword() != null && account.getConfirmPassword() != null && account.getPassword().equals(account.getConfirmPassword())) {
			entity.setPassword(Crypt.crypt(account.getPassword()));
		}
		if (account.getEnabled() != null) {
			entity.setEnabled(account.getEnabled());
		}
		entity.setUpdated(new Date());

		entity = repository.save(entity);

		return new AccountResource(entity.getId(), entity.getUsername(), entity.getDomain().getName(), 
				entity.getDomain().getId(), entity.isEnabled(), entity.getCreated(), entity.getUpdated());
	}
	
	@Override
	@Transactional
	public void delete(String username, String domainName) {
		Account account = repository.findByUsernameAndDomainName(username, domainName);
		if (account == null) {
			throw new AccountNotFoundException("invalid account");
		}
		File domainDir = new File(mailServerConfiguration.getVhostsPath(), domainName);
		FileUtils.deleteFolder(domainDir, username);
		repository.delete(account);
	}

}
