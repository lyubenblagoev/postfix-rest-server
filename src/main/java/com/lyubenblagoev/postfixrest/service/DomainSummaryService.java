package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.service.model.DomainSummaryResource;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface DomainSummaryService {

    List<DomainSummaryResource> getAllDomainSummaries();

    List<DomainSummaryResource> getAllDomainSummaries(Collection<Long> domainIds);

    Optional<DomainSummaryResource> getDomainSummaryByName(String name);
}
