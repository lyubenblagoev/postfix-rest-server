package com.lyubenblagoev.postfixrest.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyubenblagoev.postfixrest.BadRequestException;
import com.lyubenblagoev.postfixrest.entity.Account;
import com.lyubenblagoev.postfixrest.entity.AuditEntityType;
import com.lyubenblagoev.postfixrest.entity.Bcc;
import com.lyubenblagoev.postfixrest.entity.IncomingBcc;
import com.lyubenblagoev.postfixrest.entity.OutgoingBcc;
import com.lyubenblagoev.postfixrest.repository.AccountRepository;
import com.lyubenblagoev.postfixrest.repository.IncomingBccRepository;
import com.lyubenblagoev.postfixrest.repository.OutgoingBccRepository;
import com.lyubenblagoev.postfixrest.service.model.BccResource;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class BccServiceImpl extends AbstractBccServiceImpl {
	
	private final OutgoingBccRepository outBccRepository;
	private final IncomingBccRepository inBccRepository;
	private final AuditLogService auditLogService;

	public BccServiceImpl(OutgoingBccRepository outBccRepository, IncomingBccRepository inBccRepository,
			AccountRepository accountRepository, AuditLogService auditLogService) {
		super(accountRepository);
		this.outBccRepository = outBccRepository;
		this.inBccRepository = inBccRepository;
		this.auditLogService = auditLogService;
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
		boolean newEntity = entity == null;
		Map<String, ?> oldValues = bccValues(entity);
		if (entity == null) {
			entity = new OutgoingBcc();
		}
		entity = (OutgoingBcc) save(entity, resource, outBccRepository);
		recordSave(AuditEntityType.OUTGOING_BCC, entity, oldValues, newEntity);

		return Optional.of(BccResource.fromBcc(entity));
	}

	@Override
	@Transactional
	public Optional<BccResource> saveIncomingBcc(BccResource resource) {
		validateAccount(resource.getAccountId());

		IncomingBcc entity = inBccRepository.findByAccountId(resource.getAccountId());
		boolean newEntity = entity == null;
		Map<String, ?> oldValues = bccValues(entity);
		if (entity == null) {
			entity = new IncomingBcc();
		}
		entity = (IncomingBcc) save(entity, resource, inBccRepository);
		recordSave(AuditEntityType.INCOMING_BCC, entity, oldValues, newEntity);

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
		if (bcc.getId() != null) {
			Optional<IncomingBcc> existingBcc = inBccRepository.findById(bcc.getId());
			if (existingBcc.isPresent()) {
				IncomingBcc entity = existingBcc.get();
				inBccRepository.delete(entity);
				auditLogService.recordDeleted(AuditEntityType.INCOMING_BCC, entity.getId(), bccValues(entity));
			}
		}
	}

	@Override
	@Transactional
	public void deleteOutgoingBcc(BccResource bcc) {
		if (bcc.getId() != null) {
			Optional<OutgoingBcc> existingBcc = outBccRepository.findById(bcc.getId());
			if (existingBcc.isPresent()) {
				OutgoingBcc entity = existingBcc.get();
				outBccRepository.delete(entity);
				auditLogService.recordDeleted(AuditEntityType.OUTGOING_BCC, entity.getId(), bccValues(entity));
			}
		}
	}

	private void recordSave(AuditEntityType entityType, Bcc entity, Map<String, ?> oldValues, boolean newEntity) {
		Map<String, ?> newValues = bccValues(entity);
		if (newEntity) {
			auditLogService.recordCreated(entityType, entity.getId(), newValues);
		} else {
			auditLogService.recordUpdated(entityType, entity.getId(), oldValues, newValues);
		}
	}

	private Map<String, ?> bccValues(Bcc bcc) {
		Map<String, Object> values = new LinkedHashMap<>();
		if (bcc == null) {
			return values;
		}
		Account account = bcc.getAccount();
		values.put("accountId", account == null ? null : account.getId());
		values.put("domainId", account == null || account.getDomain() == null ? null : account.getDomain().getId());
		values.put("email", bcc.getReceiverEmailAddress());
		values.put("enabled", bcc.isEnabled());
		return values;
	}
	
}
