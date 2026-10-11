package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.repository.AccountRepository;
import com.lyubenblagoev.postfixrest.repository.DomainRepository;
import com.lyubenblagoev.postfixrest.repository.OutgoingBccRepository;
import com.lyubenblagoev.postfixrest.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuditLogControllerIntegrationTest {

    private static final String DOMAINS_PATH = "/api/v1/domains";
    private static final String DOMAIN_NAME = "audit-trail.example.com";

    private final MockMvc mockMvc;
    private final JwtTokenProvider jwtTokenProvider;
    private final DomainRepository domainRepository;
    private final AccountRepository accountRepository;
    private final OutgoingBccRepository outgoingBccRepository;

    @Autowired
    AuditLogControllerIntegrationTest(MockMvc mockMvc, JwtTokenProvider jwtTokenProvider,
                                      DomainRepository domainRepository, AccountRepository accountRepository,
                                      OutgoingBccRepository outgoingBccRepository) {
        this.mockMvc = mockMvc;
        this.jwtTokenProvider = jwtTokenProvider;
        this.domainRepository = domainRepository;
        this.accountRepository = accountRepository;
        this.outgoingBccRepository = outgoingBccRepository;
    }

    @Test
    void query_shouldReturnFieldHistoryForAnEntity() throws Exception {
        String token = jwtTokenProvider.createToken("admin@example.com", Collections.emptyList());
        String authorization = "Bearer " + token;

        mockMvc.perform(post(DOMAINS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authorization)
                        .content("{ \"name\": \"" + DOMAIN_NAME + "\", \"enabled\": true }"))
                .andExpect(status().isCreated());

        Long domainId = domainRepository.findByName(DOMAIN_NAME).orElseThrow().getId();
        mockMvc.perform(put(DOMAINS_PATH + "/" + DOMAIN_NAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authorization)
                        .content("{ \"name\": \"" + DOMAIN_NAME + "\", \"enabled\": false }"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/audit-logs")
                        .param("entityType", "DOMAIN")
                        .param("entityId", domainId.toString())
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].action", is("UPDATED")))
                .andExpect(jsonPath("$[0].entityType", is("DOMAIN")))
                .andExpect(jsonPath("$[0].entityId", is(domainId.intValue())))
                .andExpect(jsonPath("$[0].resourceIdentifier", is(DOMAIN_NAME)))
                .andExpect(jsonPath("$[0].user", is("admin@example.com")))
                .andExpect(jsonPath("$[0].changes", hasSize(1)))
                .andExpect(jsonPath("$[0].changes[0].field", is("enabled")))
                .andExpect(jsonPath("$[0].changes[0].oldValue", is("true")))
                .andExpect(jsonPath("$[0].changes[0].newValue", is("false")));
    }

    @Test
    void query_withDomainScope_shouldReturnRelatedActivityAndHistoricalBccContext() throws Exception {
        String token = jwtTokenProvider.createToken("admin@example.com", Collections.emptyList());
        String authorization = "Bearer " + token;
        String domainName = "scoped-audit.example.com";
        String accountName = "alice";

        mockMvc.perform(post(DOMAINS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authorization)
                        .content("{ \"name\": \"" + domainName + "\", \"enabled\": true }"))
                .andExpect(status().isCreated());

        Long domainId = domainRepository.findByName(domainName).orElseThrow().getId();
        mockMvc.perform(post(DOMAINS_PATH + "/" + domainName + "/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authorization)
                        .content("{ \"username\": \"" + accountName + "\", \"enabled\": true }"))
                .andExpect(status().isOk());

        Long accountId = accountRepository.findByUsernameAndDomainName(accountName, domainName).orElseThrow().getId();
        mockMvc.perform(post(DOMAINS_PATH + "/" + domainName + "/aliases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authorization)
                        .content("{ \"name\": \"info\", \"email\": \"target@example.net\", \"enabled\": true }"))
                .andExpect(status().isOk());
        mockMvc.perform(put(DOMAINS_PATH + "/" + domainName + "/aliases/info/target@example.net")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authorization)
                        .content("{ \"name\": \"support\", \"email\": \"destination@example.net\", \"enabled\": true }"))
                .andExpect(status().isOk());
        mockMvc.perform(post(DOMAINS_PATH + "/" + domainName + "/accounts/" + accountName + "/bccs/incoming")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authorization)
                        .content("{ \"email\": \"incoming@example.net\", \"enabled\": true }"))
                .andExpect(status().isOk());
        mockMvc.perform(post(DOMAINS_PATH + "/" + domainName + "/accounts/" + accountName + "/bccs/outgoing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authorization)
                        .content("{ \"email\": \"outgoing@example.net\", \"enabled\": true }"))
                .andExpect(status().isOk());

        mockMvc.perform(put(DOMAINS_PATH + "/" + domainName + "/accounts/" + accountName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authorization)
                        .content("{ \"username\": \"alice-renamed\", \"enabled\": true }"))
                .andExpect(status().isOk());

        mockMvc.perform(delete(DOMAINS_PATH + "/" + domainName + "/aliases/support/destination@example.net")
                        .header("Authorization", authorization))
                .andExpect(status().isOk());

        Long outgoingBccId = outgoingBccRepository.findByAccountId(accountId).getId();
        mockMvc.perform(delete(DOMAINS_PATH + "/" + domainName + "/accounts/alice-renamed/bccs/outgoing")
                        .header("Authorization", authorization))
                .andExpect(status().isOk());
        mockMvc.perform(delete(DOMAINS_PATH + "/" + domainName + "/accounts/alice-renamed/bccs/incoming")
                        .header("Authorization", authorization))
                .andExpect(status().isOk());
        mockMvc.perform(delete(DOMAINS_PATH + "/" + domainName + "/accounts/alice-renamed")
                        .header("Authorization", authorization))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/audit-logs")
                        .param("scope", "DOMAIN")
                        .param("scopeId", domainId.toString())
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[*].entityType", containsInAnyOrder(
                        "ACCOUNT", "ACCOUNT", "ACCOUNT", "ALIAS", "ALIAS", "ALIAS",
                        "INCOMING_BCC", "INCOMING_BCC", "OUTGOING_BCC", "OUTGOING_BCC")))
                .andExpect(jsonPath("$[0].action", is("DELETED")))
                .andExpect(jsonPath("$[0].resourceIdentifier", is("alice-renamed@" + domainName)))
                .andExpect(jsonPath("$[*].resourceIdentifier", hasItems(
                        accountName + "@" + domainName,
                        "alice-renamed@" + domainName,
                        "info@" + domainName + " -> target@example.net",
                        "support@" + domainName + " -> destination@example.net",
                        "incoming@example.net", "outgoing@example.net")))
                .andExpect(jsonPath("$[2].entityId", is(outgoingBccId.intValue())))
                .andExpect(jsonPath("$[2].resourceIdentifier", is("outgoing@example.net")))
                .andExpect(jsonPath("$[2].changes[*].field", hasItems("accountId", "domainId")));

        mockMvc.perform(get("/api/v1/audit-logs")
                        .param("scope", "ACCOUNT")
                        .param("scopeId", accountId.toString())
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[*].entityType", containsInAnyOrder(
                        "ACCOUNT", "ACCOUNT", "ACCOUNT", "INCOMING_BCC", "INCOMING_BCC",
                        "OUTGOING_BCC", "OUTGOING_BCC")))
                .andExpect(jsonPath("$[*].entityType", hasItems("ACCOUNT", "INCOMING_BCC", "OUTGOING_BCC")))
                .andExpect(jsonPath("$[*].resourceIdentifier", hasItems(
                        "alice-renamed@" + domainName, "incoming@example.net", "outgoing@example.net")));
    }
}
