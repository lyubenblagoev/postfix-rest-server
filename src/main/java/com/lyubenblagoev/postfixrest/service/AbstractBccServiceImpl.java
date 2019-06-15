package com.lyubenblagoev.postfixrest.service;

import java.util.Date;
import java.util.Optional;

import com.lyubenblagoev.postfixrest.BadRequestException;
import com.lyubenblagoev.postfixrest.entity.Account;
import com.lyubenblagoev.postfixrest.entity.Bcc;
import com.lyubenblagoev.postfixrest.repository.AccountRepository;
import com.lyubenblagoev.postfixrest.repository.BccRepository;
import com.lyubenblagoev.postfixrest.service.model.BccResource;

public abstract class AbstractBccServiceImpl implements BccService {
	
	protected final AccountRepository accountRepository;
	
	AbstractBccServiceImpl(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@SuppressWarnings({"rawtypes"})
	protected Optional<BccResource> getBccResource(String domain, String account, BccRepository repo) {
		Bcc entity = repo.findByAccountDomainNameAndAccountUsername(domain, account);
		return Optional.ofNullable(entity).map(BccResource::fromBcc);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes" })
	protected Bcc save(Bcc entity, BccResource resource, BccRepository repository)  {
		if (resource.getId() == null && repository.findByAccountId(resource.getAccountId()) != null) {
			// POST request, do not allow users to create more than one record for each account
			throw new BadRequestException("cannot create more than one BCC address for an account");
		} else if (resource.getId() != null && !repository.findById(resource.getId()).isPresent()) {
			throw new BadRequestException("cannot update non-existing BCC record");
		}
		return saveResource(resource, entity, repository);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Bcc saveResource(BccResource resource, Bcc entity, BccRepository repo) {
		entity = validateAndCopyProps(entity, resource);
		repo.save(entity);
		return entity;
	}
	
	protected Bcc validateAndCopyProps(Bcc entity, BccResource resource) {
		Account account = accountRepository.findById(resource.getAccountId()).orElse(null);
		if (account == null) {
			throw new BadRequestException("account with id " + resource.getAccountId() + " not found");
		}
		entity.setAccount(account);
		entity.setReceiverEmailAddress(resource.getEmail());
		entity.setEnabled(resource.isEnabled());
		entity.setUpdated(new Date());
		return entity;
	}

}
