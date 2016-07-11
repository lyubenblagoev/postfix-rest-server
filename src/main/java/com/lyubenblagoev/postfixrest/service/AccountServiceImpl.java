package com.lyubenblagoev.postfixrest.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.Crypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Override
	public List<AccountResource> getAllAccounts() {
		Iterable<Account> entities = repository.findAll();
		List<AccountResource> accounts = new ArrayList<>();
		entities.forEach(e -> accounts.add(new AccountResource(e.getId(), e.getUsername(), e.getDomain().getName(), 
				e.getDomain().getId(), e.isEnabled(), e.getCreated(), e.getUpdated())));
		return accounts;
	}

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
	public List<AccountResource> getAccountsByDomainId(Long id) {
		List<Account> entities = repository.findByDomainId(id);
		List<AccountResource> accounts = new ArrayList<>(entities.size());
		entities.forEach(e -> accounts.add(new AccountResource(e.getId(), e.getUsername(), e.getDomain().getName(), 
				e.getDomain().getId(), e.isEnabled(), e.getCreated(), e.getUpdated())));
		return accounts;
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
		if (account.getId() == null && repository.findByUsernameAndDomainId(account.getUsername(), account.getDomainId()) != null) {
			throw new AccountExistsException("another account with that name already exists");
		}
		entity.setUsername(account.getUsername());
		if (account.getPassword() != null && account.getConfirmPassword() != null
				&& account.getPassword().equals(account.getConfirmPassword())) {
			entity.setPassword(Crypt.crypt(account.getPassword()));
		}
		entity = repository.save(entity);
		return new AccountResource(entity.getId(), entity.getUsername(), entity.getDomain().getName(), 
				entity.getDomain().getId(), entity.isEnabled(), entity.getCreated(), entity.getUpdated());
	}
	
	@Override
	@Transactional
	public void delete(Long id) {
		if (!repository.exists(id)) {
			throw new AccountNotFoundException("no account with id " + id);
		}
		repository.delete(id);
	}

}
