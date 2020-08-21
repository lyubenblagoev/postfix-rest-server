package com.lyubenblagoev.postfixrest.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyubenblagoev.postfixrest.BadRequestException;
import com.lyubenblagoev.postfixrest.entity.Account;
import com.lyubenblagoev.postfixrest.entity.IncomingBcc;
import com.lyubenblagoev.postfixrest.entity.OutgoingBcc;
import com.lyubenblagoev.postfixrest.repository.AccountRepository;
import com.lyubenblagoev.postfixrest.repository.IncomingBccRepository;
import com.lyubenblagoev.postfixrest.repository.OutgoingBccRepository;
import com.lyubenblagoev.postfixrest.service.model.BccResource;

@Service
@Transactional(readOnly = true)
public class BccServiceImpl extends AbstractBccServiceImpl {
	
	private final OutgoingBccRepository outBccRepository;
	private final IncomingBccRepository inBccRepository;

	public BccServiceImpl(OutgoingBccRepository outBccRepository, IncomingBccRepository inBccRepository,
			AccountRepository accountRepository) {
		super(accountRepository);
		this.outBccRepository = outBccRepository;
		this.inBccRepository = inBccRepository;
	}
	
	@Override
	public Optional<BccResource> getOutgoingBcc(String domain, String username) {
		return getBccResource(domain, username, outBccRepository);
	}

	@Override
	public Optional<BccResource> getIncomingBcc(String domain, String username) {
		return getBccResource(domain, username, inBccRepository);
	}

	@Override
	@Transactional
	public Optional<BccResource> saveOutgoingBcc(BccResource resource) {
		validateAccount(resource.getAccountId());

		OutgoingBcc entity = outBccRepository.findByAccountId(resource.getAccountId());
		if (entity == null) {
			entity = new OutgoingBcc();
		}
		entity = (OutgoingBcc) save(entity, resource, outBccRepository);

		return Optional.of(BccResource.fromBcc(entity));
	}

	@Override
	@Transactional
	public Optional<BccResource> saveIncomingBcc(BccResource resource) {
		validateAccount(resource.getAccountId());

		IncomingBcc entity = inBccRepository.findByAccountId(resource.getAccountId());
		if (entity == null) {
			entity = new IncomingBcc();
		}
		entity = (IncomingBcc) save(entity, resource, inBccRepository);

		return Optional.of(BccResource.fromBcc(entity));
	}
	
	private void validateAccount(Long accountId) {
		Optional<Account> account = accountRepository.findById(accountId);
		if (!account.isPresent()) {
			throw new BadRequestException("account with id " + accountId + " not found");
		}
	}
	
	@Override
	@Transactional
	public void deleteIncomingBcc(BccResource bcc) {
		Optional<Account> account = accountRepository.findById(bcc.getAccountId());
		if (account.isPresent()) {
			inBccRepository.delete((IncomingBcc) BccResource.toBcc(bcc, account.get()));
		}
	}

	@Override
	@Transactional
	public void deleteOutgoingBcc(BccResource bcc) {
		Optional<Account> account = accountRepository.findById(bcc.getAccountId());
		if (account.isPresent()) {
			outBccRepository.delete((OutgoingBcc) BccResource.toBcc(bcc, account.get()));
		}
	}
	
}
