package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.entity.Domain;
import com.lyubenblagoev.postfixrest.repository.AccountRepository;
import com.lyubenblagoev.postfixrest.repository.AliasRepository;
import com.lyubenblagoev.postfixrest.repository.DomainAccountAggregate;
import com.lyubenblagoev.postfixrest.repository.DomainAliasAggregate;
import com.lyubenblagoev.postfixrest.repository.DomainRepository;
import com.lyubenblagoev.postfixrest.service.model.DomainSummaryResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class DomainSummaryServiceImplTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AliasRepository aliasRepository;

    @Test
    void getAllDomainSummaries_shouldAggregateWithoutLoadingResourcesPerDomain() {
        Domain alpha = domain(1L, "alpha.example.com", true);
        Domain zulu = domain(2L, "zulu.example.com", false);
        when(domainRepository.findAllByOrderByNameAsc()).thenReturn(List.of(alpha, zulu));

        DomainAccountAggregate alphaAccounts = accountAggregate(1L, 2L, 1L, 1L);
        DomainAccountAggregate zuluAccounts = accountAggregate(2L, 1L, 0L, 1L);
        when(accountRepository.findAggregatesByDomain()).thenReturn(List.of(alphaAccounts, zuluAccounts));

        DomainAliasAggregate healthy = aliasAggregate(1L, 2L, 0L);
        DomainAliasAggregate paused = aliasAggregate(1L, 0L, 1L);
        DomainAliasAggregate mixed = aliasAggregate(1L, 1L, 1L);
        when(aliasRepository.findAggregatesByDomain()).thenReturn(List.of(healthy, paused, mixed));

        List<DomainSummaryResource> summaries = new DomainSummaryServiceImpl(
                domainRepository, accountRepository, aliasRepository).getAllDomainSummaries();

        assertEquals(List.of("alpha.example.com", "zulu.example.com"),
                summaries.stream().map(DomainSummaryResource::getName).toList());
        assertEquals(2, summaries.get(0).getAccounts().getTotal());
        assertEquals(1, summaries.get(0).getAccounts().getEnabled());
        assertEquals(1, summaries.get(0).getAccounts().getDisabled());
        assertEquals(3, summaries.get(0).getAliases().getTotal());
        assertEquals(1, summaries.get(0).getAliases().getHealthy());
        assertEquals(1, summaries.get(0).getAliases().getNeedsReview());
        assertEquals(1, summaries.get(0).getAliases().getPaused());
        assertEquals(1, summaries.get(1).getAccounts().getTotal());
        assertEquals(0, summaries.get(1).getAliases().getTotal());
        assertEquals(0, summaries.get(1).getAliases().getHealthy());
        assertEquals(0, summaries.get(1).getAliases().getNeedsReview());
        assertEquals(0, summaries.get(1).getAliases().getPaused());

        verify(accountRepository).findAggregatesByDomain();
        verify(aliasRepository).findAggregatesByDomain();
        verify(accountRepository, never()).findByDomainName(anyString());
        verify(aliasRepository, never()).findByDomainName(anyString());
    }

    private Domain domain(Long id, String name, boolean enabled) {
        Domain domain = new Domain();
        domain.setId(id);
        domain.setName(name);
        domain.setEnabled(enabled);
        return domain;
    }

    private DomainAccountAggregate accountAggregate(Long domainId, Long total, Long enabled, Long disabled) {
        DomainAccountAggregate aggregate = org.mockito.Mockito.mock(DomainAccountAggregate.class);
        when(aggregate.getDomainId()).thenReturn(domainId);
        when(aggregate.getTotal()).thenReturn(total);
        when(aggregate.getEnabled()).thenReturn(enabled);
        when(aggregate.getDisabled()).thenReturn(disabled);
        return aggregate;
    }

    private DomainAliasAggregate aliasAggregate(Long domainId, Long enabledRecipients, Long disabledRecipients) {
        DomainAliasAggregate aggregate = org.mockito.Mockito.mock(DomainAliasAggregate.class);
        when(aggregate.getDomainId()).thenReturn(domainId);
        when(aggregate.getEnabledRecipients()).thenReturn(enabledRecipients);
        when(aggregate.getDisabledRecipients()).thenReturn(disabledRecipients);
        return aggregate;
    }
}
