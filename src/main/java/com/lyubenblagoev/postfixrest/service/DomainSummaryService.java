package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.service.model.DomainSummaryResource;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DomainSummaryService {

    List<DomainSummaryResource> getAllDomainSummaries();

    List<DomainSummaryResource> getAllDomainSummaries(Collection<Long> domainIds);

    Page<DomainSummaryResource> getAllDomainSummaries(Collection<Long> domainIds, Pageable pageable);

    Optional<DomainSummaryResource> getDomainSummaryByName(String name);
}
