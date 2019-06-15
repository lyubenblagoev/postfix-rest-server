package com.lyubenblagoev.postfixrest.service;

import java.util.Optional;

import com.lyubenblagoev.postfixrest.service.model.BccResource;

public interface BccService {
	
	Optional<BccResource> getOutgoingBcc(String domain, String username);

	Optional<BccResource> saveOutgoingBcc(BccResource bcc);

	Optional<BccResource> getIncommingBcc(String domain, String username);

	Optional<BccResource> saveIncommingBcc(BccResource bcc);

	void deleteOutgoingBcc(BccResource bcc);

	void deleteIncommingBcc(BccResource bcc);
	
}
