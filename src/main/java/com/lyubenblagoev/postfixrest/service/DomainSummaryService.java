package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.service.model.DomainSummaryResource;

import java.util.List;
import java.util.Optional;

public interface DomainSummaryService {

    List<DomainSummaryResource> getAllDomainSummaries();

    Optional<DomainSummaryResource> getDomainSummaryByName(String name);
}
