package com.lyubenblagoev.postfixrest.repository;

import com.lyubenblagoev.postfixrest.entity.OutgoingBcc;

public interface OutgoingBccRepository extends BccRepository<OutgoingBcc, Long> {
	@Override
	OutgoingBcc findByAccountId(Long accountId);
}
