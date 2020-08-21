package com.lyubenblagoev.postfixrest.repository;

import com.lyubenblagoev.postfixrest.entity.IncomingBcc;

public interface IncomingBccRepository extends BccRepository<IncomingBcc, Long> {
	
	@Override
	IncomingBcc findByAccountId(Long accountId);
	
}
