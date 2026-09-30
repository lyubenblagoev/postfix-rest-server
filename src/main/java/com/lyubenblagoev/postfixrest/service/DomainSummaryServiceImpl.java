package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.entity.Domain;
import com.lyubenblagoev.postfixrest.repository.AccountRepository;
import com.lyubenblagoev.postfixrest.repository.AliasRepository;
import com.lyubenblagoev.postfixrest.repository.DomainAccountAggregate;
import com.lyubenblagoev.postfixrest.repository.DomainAliasAggregate;
import com.lyubenblagoev.postfixrest.repository.DomainRepository;
import com.lyubenblagoev.postfixrest.service.model.DomainSummaryResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DomainSummaryServiceImpl implements DomainSummaryService {

    private final DomainRepository domainRepository;
    private final AccountRepository accountRepository;
    private final AliasRepository aliasRepository;

    public DomainSummaryServiceImpl(DomainRepository domainRepository, AccountRepository accountRepository,
                                    AliasRepository aliasRepository) {
        this.domainRepository = domainRepository;
        this.accountRepository = accountRepository;
        this.aliasRepository = aliasRepository;
    }

    @Override
    public List<DomainSummaryResource> getAllDomainSummaries() {
        List<Domain> domains = domainRepository.findAllByOrderByNameAsc();
        Map<Long, DomainSummaryResource.AccountSummary> accounts = accountSummaries(
                accountRepository.findAggregatesByDomain());
        Map<Long, DomainSummaryResource.AliasSummary> aliases = aliasSummaries(
                aliasRepository.findAggregatesByDomain());
        return domains.stream()
                .map(domain -> toResource(domain, accounts, aliases))
                .collect(Collectors.toList());
    }

    @Override
    public List<DomainSummaryResource> getAllDomainSummaries(Collection<Long> domainIds) {
        if (domainIds == null) {
            return getAllDomainSummaries();
        }
        if (domainIds.isEmpty()) {
            return List.of();
        }
        List<Domain> domains = domainRepository.findAllByIdInOrderByNameAsc(domainIds);
        Map<Long, DomainSummaryResource.AccountSummary> accounts = accountSummaries(
                accountRepository.findAggregatesByDomainIds(domainIds));
        Map<Long, DomainSummaryResource.AliasSummary> aliases = aliasSummaries(
                aliasRepository.findAggregatesByDomainIds(domainIds));
        return domains.stream().map(domain -> toResource(domain, accounts, aliases)).collect(Collectors.toList());
    }

    @Override
    public Optional<DomainSummaryResource> getDomainSummaryByName(String name) {
        return domainRepository.findByName(name).map(domain -> {
            Map<Long, DomainSummaryResource.AccountSummary> accounts = accountSummaries(
                    accountRepository.findAggregatesByDomainId(domain.getId()));
            Map<Long, DomainSummaryResource.AliasSummary> aliases = aliasSummaries(
                    aliasRepository.findAggregatesByDomainId(domain.getId()));
            return toResource(domain, accounts, aliases);
        });
    }

    private DomainSummaryResource toResource(Domain domain,
                                             Map<Long, DomainSummaryResource.AccountSummary> accounts,
                                             Map<Long, DomainSummaryResource.AliasSummary> aliases) {
        DomainSummaryResource.AccountSummary accountSummary = accounts.getOrDefault(domain.getId(),
                new DomainSummaryResource.AccountSummary(0, 0, 0));
        DomainSummaryResource.AliasSummary aliasSummary = aliases.getOrDefault(domain.getId(),
                new DomainSummaryResource.AliasSummary(0, 0, 0, 0));
        return DomainSummaryResource.fromDomain(domain, accountSummary, aliasSummary);
    }

    private Map<Long, DomainSummaryResource.AccountSummary> accountSummaries(List<DomainAccountAggregate> aggregates) {
        Map<Long, DomainSummaryResource.AccountSummary> summaries = new HashMap<>();
        for (DomainAccountAggregate aggregate : aggregates) {
            summaries.put(aggregate.getDomainId(), new DomainSummaryResource.AccountSummary(
                    valueOrZero(aggregate.getTotal()), valueOrZero(aggregate.getEnabled()),
                    valueOrZero(aggregate.getDisabled())));
        }
        return summaries;
    }

    private Map<Long, DomainSummaryResource.AliasSummary> aliasSummaries(List<DomainAliasAggregate> aggregates) {
        Map<Long, AliasCounts> countsByDomain = new HashMap<>();
        for (DomainAliasAggregate aggregate : aggregates) {
            long enabledRecipients = valueOrZero(aggregate.getEnabledRecipients());
            long disabledRecipients = valueOrZero(aggregate.getDisabledRecipients());
            AliasCounts counts = countsByDomain.computeIfAbsent(aggregate.getDomainId(), ignored -> new AliasCounts());
            counts.total++;
            if (enabledRecipients > 0 && disabledRecipients > 0) {
                counts.needsReview++;
            } else if (enabledRecipients > 0) {
                counts.healthy++;
            } else {
                counts.paused++;
            }
        }

        Map<Long, DomainSummaryResource.AliasSummary> summaries = new HashMap<>();
        countsByDomain.forEach((domainId, counts) -> summaries.put(domainId,
                new DomainSummaryResource.AliasSummary(counts.total, counts.healthy,
                        counts.needsReview, counts.paused)));
        return summaries;
    }

    private long valueOrZero(Long value) {
        return value == null ? 0 : value;
    }

    private static class AliasCounts {
        private long total;
        private long healthy;
        private long needsReview;
        private long paused;
    }
}
