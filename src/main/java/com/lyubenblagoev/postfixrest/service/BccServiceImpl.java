package com.lyubenblagoev.postfixrest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyubenblagoev.postfixrest.entity.Account;
import com.lyubenblagoev.postfixrest.entity.IncommingBcc;
import com.lyubenblagoev.postfixrest.entity.OutgoingBcc;
import com.lyubenblagoev.postfixrest.repository.AccountRepository;
import com.lyubenblagoev.postfixrest.repository.IncommingBccRepository;
import com.lyubenblagoev.postfixrest.repository.OutgoingBccRepository;
import com.lyubenblagoev.postfixrest.service.model.BccResource;

@Service
@Transactional(readOnly = true)
public class BccServiceImpl extends AbstractBccServiceImpl {
	
	@Autowired
	private OutgoingBccRepository outBccRepository;

	@Autowired
	private IncommingBccRepository inBccRepository;
	
	@Autowired
	private AccountRepository accountRepository;

	@Override
	public BccResource getOutgoingBcc(Long id) {
		return getBccResource(id, outBccRepository);
	}

	@Override
	public BccResource getIncommingBcc(Long id) {
		return getBccResource(id, inBccRepository);
	}

	@Override
	@Transactional
	public BccResource saveOutgoingBcc(BccResource resource) {
		validateAccount(resource.getAccountId());
		OutgoingBcc entity = outBccRepository.findByAccountId(resource.getAccountId());
		if (entity == null) {
			entity = new OutgoingBcc();
		}
		entity = (OutgoingBcc) save(entity, resource, outBccRepository);
		return new BccResource(entity.getId(), entity.getAccount().getId(), entity.getReceiverEmailAddress(),
				entity.isEnabled(), entity.getCreated(), entity.getUpdated());
	}

	@Override
	@Transactional
	public BccResource saveIncommingBcc(BccResource resource) {
		validateAccount(resource.getAccountId());
		IncommingBcc entity = inBccRepository.findByAccountId(resource.getAccountId());
		if (entity == null) {
			entity = new IncommingBcc();
		}
		entity = (IncommingBcc) save(entity, resource, inBccRepository);
		return new BccResource(entity.getId(), entity.getAccount().getId(), entity.getReceiverEmailAddress(),
				entity.isEnabled(), entity.getCreated(), entity.getUpdated());
	}
	
	private void validateAccount(Long accountId) {
		Account account = accountRepository.findOne(accountId);
		if (account == null) {
			throw new BadRequestException("account with id " + accountId + " not found");
		}
	}
	
	@Override
	@Transactional
	public void deleteIncommingBcc(Long id) {
		if (inBccRepository.findOne(id) == null) {
			throw new BccNotFoundException("BCC with id " + id + " not found");
		}
		inBccRepository.delete(id);
	}

	@Override
	@Transactional
	public void deleteOutgoingBcc(Long id) {
		if (outBccRepository.findOne(id) == null) {
			throw new BccNotFoundException("BCC with id " + id + " not found");
		}
		outBccRepository.delete(id);
	}
	
}
