package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.service.model.BccResource;

public interface BccService {
	BccResource getOutgoingBcc(Long id);
	BccResource saveOutgoingBcc(BccResource bcc);
	BccResource getIncommingBcc(Long id);
	BccResource saveIncommingBcc(BccResource bcc);
	void deleteOutgoingBcc(Long id);
	void deleteIncommingBcc(Long id);
}
