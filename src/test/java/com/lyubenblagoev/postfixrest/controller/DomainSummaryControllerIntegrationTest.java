package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.entity.Account;
import com.lyubenblagoev.postfixrest.entity.Alias;
import com.lyubenblagoev.postfixrest.entity.Domain;
import com.lyubenblagoev.postfixrest.repository.AccountRepository;
import com.lyubenblagoev.postfixrest.repository.AliasRepository;
import com.lyubenblagoev.postfixrest.repository.DomainRepository;
import com.lyubenblagoev.postfixrest.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DomainSummaryControllerIntegrationTest {

    private final MockMvc mockMvc;
    private final JwtTokenProvider jwtTokenProvider;
    private final DomainRepository domainRepository;
    private final AccountRepository accountRepository;
    private final AliasRepository aliasRepository;

    private String authorization;
    private Long zuluDomainId;

    @Autowired
    DomainSummaryControllerIntegrationTest(MockMvc mockMvc, JwtTokenProvider jwtTokenProvider,
                                            DomainRepository domainRepository, AccountRepository accountRepository,
                                            AliasRepository aliasRepository) {
        this.mockMvc = mockMvc;
        this.jwtTokenProvider = jwtTokenProvider;
        this.domainRepository = domainRepository;
        this.accountRepository = accountRepository;
        this.aliasRepository = aliasRepository;
    }

    @BeforeEach
    void setUp() {
        aliasRepository.deleteAll();
        accountRepository.deleteAll();
        domainRepository.deleteAll();
        authorization = "Bearer " + jwtTokenProvider.createToken("admin@example.com", Collections.emptyList());

        saveDomain("alpha-summary.example.com", true);
        Domain zulu = saveDomain("zulu-summary.example.com", false);
        zuluDomainId = zulu.getId();

        saveAccount(zulu, "alice", true);
        saveAccount(zulu, "bob", false);

        saveAlias(zulu, "healthy", "one@example.net", true);
        saveAlias(zulu, "healthy", "two@example.net", true);
        saveAlias(zulu, "paused", "paused@example.net", false);
        saveAlias(zulu, "mixed", "enabled@example.net", true);
        saveAlias(zulu, "mixed", "disabled@example.net", false);
    }

    @Test
    void list_shouldReturnOrderedAggregateSummariesWithoutResourceData() throws Exception {
        mockMvc.perform(get("/api/v1/domain-summaries").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("alpha-summary.example.com")))
                .andExpect(jsonPath("$[0].accounts.total", is(0)))
                .andExpect(jsonPath("$[0].accounts.enabled", is(0)))
                .andExpect(jsonPath("$[0].accounts.disabled", is(0)))
                .andExpect(jsonPath("$[0].aliases.total", is(0)))
                .andExpect(jsonPath("$[0].aliases.healthy", is(0)))
                .andExpect(jsonPath("$[0].aliases.needsReview", is(0)))
                .andExpect(jsonPath("$[0].aliases.paused", is(0)))
                .andExpect(jsonPath("$[1].name", is("zulu-summary.example.com")))
                .andExpect(jsonPath("$[1].accounts.total", is(2)))
                .andExpect(jsonPath("$[1].accounts.enabled", is(1)))
                .andExpect(jsonPath("$[1].accounts.disabled", is(1)))
                .andExpect(jsonPath("$[1].aliases.total", is(3)))
                .andExpect(jsonPath("$[1].aliases.healthy", is(1)))
                .andExpect(jsonPath("$[1].aliases.needsReview", is(1)))
                .andExpect(jsonPath("$[1].aliases.paused", is(1)))
                .andExpect(content().string(not(containsString("alice"))))
                .andExpect(content().string(not(containsString("enabled@example.net"))))
                .andExpect(jsonPath("$[1].accounts.accountIds").doesNotExist())
                .andExpect(jsonPath("$[1].aliases.aliasIds").doesNotExist());
    }

    @Test
    void get_shouldReturnOneSummaryAndRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/domain-summaries/zulu-summary.example.com")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(zuluDomainId.intValue())))
                .andExpect(jsonPath("$.accounts.total", is(2)))
                .andExpect(jsonPath("$.aliases.total", is(3)));

        mockMvc.perform(get("/api/v1/domain-summaries/unknown-summary.example.com")
                        .header("Authorization", authorization))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/domain-summaries"))
                .andExpect(status().isForbidden());
    }

    private Domain saveDomain(String name, boolean enabled) {
        Domain domain = new Domain();
        domain.setName(name);
        domain.setEnabled(enabled);
        return domainRepository.save(domain);
    }

    private void saveAccount(Domain domain, String username, boolean enabled) {
        Account account = new Account();
        account.setDomain(domain);
        account.setUsername(username);
        account.setEnabled(enabled);
        accountRepository.save(account);
    }

    private void saveAlias(Domain domain, String name, String email, boolean enabled) {
        Alias alias = new Alias();
        alias.setDomain(domain);
        alias.setAlias(name);
        alias.setEmail(email);
        alias.setEnabled(enabled);
        aliasRepository.save(alias);
    }
}
