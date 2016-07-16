package com.lyubenblagoev.postfixrest.repository;

import com.lyubenblagoev.postfixrest.entity.IncommingBcc;

public interface IncommingBccRepository extends BccRepository<IncommingBcc, Long> {
	@Override
	IncommingBcc findByAccountId(Long accountId);
}
