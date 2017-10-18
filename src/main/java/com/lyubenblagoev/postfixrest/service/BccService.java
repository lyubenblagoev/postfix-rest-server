package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.service.model.BccResource;

public interface BccService {
	
	BccResource getOutgoingBcc(String domain, String username);

	BccResource saveOutgoingBcc(BccResource bcc);

	BccResource getIncommingBcc(String domain, String username);

	BccResource saveIncommingBcc(BccResource bcc);

	void deleteOutgoingBcc(String domain, String username);

	void deleteIncommingBcc(String domain, String username);
	
}
