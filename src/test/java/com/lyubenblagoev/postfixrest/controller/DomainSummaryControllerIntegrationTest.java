package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.entity.Account;
import com.lyubenblagoev.postfixrest.entity.Alias;
import com.lyubenblagoev.postfixrest.entity.Domain;
import com.lyubenblagoev.postfixrest.entity.Role;
import com.lyubenblagoev.postfixrest.entity.User;
import com.lyubenblagoev.postfixrest.entity.Workspace;
import com.lyubenblagoev.postfixrest.entity.WorkspaceMapping;
import com.lyubenblagoev.postfixrest.repository.AccountRepository;
import com.lyubenblagoev.postfixrest.repository.AliasRepository;
import com.lyubenblagoev.postfixrest.repository.DomainRepository;
import com.lyubenblagoev.postfixrest.repository.RoleRepository;
import com.lyubenblagoev.postfixrest.repository.UserRepository;
import com.lyubenblagoev.postfixrest.repository.WorkspaceMappingRepository;
import com.lyubenblagoev.postfixrest.repository.WorkspaceRepository;
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
import java.util.List;

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
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMappingRepository workspaceMappingRepository;

    private String authorization;
    private Long zuluDomainId;

    @Autowired
    DomainSummaryControllerIntegrationTest(MockMvc mockMvc, JwtTokenProvider jwtTokenProvider,
                                           DomainRepository domainRepository, AccountRepository accountRepository,
                                           AliasRepository aliasRepository, UserRepository userRepository,
                                           RoleRepository roleRepository, WorkspaceRepository workspaceRepository,
                                           WorkspaceMappingRepository workspaceMappingRepository) {
        this.mockMvc = mockMvc;
        this.jwtTokenProvider = jwtTokenProvider;
        this.domainRepository = domainRepository;
        this.accountRepository = accountRepository;
        this.aliasRepository = aliasRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMappingRepository = workspaceMappingRepository;
    }

    @BeforeEach
    void setUp() {
        workspaceRepository.findByName("summary-pagination-first")
                .ifPresent(workspaceRepository::delete);
        workspaceRepository.findByName("summary-pagination-second")
                .ifPresent(workspaceRepository::delete);
        userRepository.findByEmail("z-pagination-member@example.com")
                .ifPresent(userRepository::delete);
        userRepository.findByEmail("summary-pagination-member@example.com")
                .ifPresent(userRepository::delete);
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
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.pageSize", is(50)))
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.items[0].name", is("alpha-summary.example.com")))
                .andExpect(jsonPath("$.items[0].accounts.total", is(0)))
                .andExpect(jsonPath("$.items[0].accounts.enabled", is(0)))
                .andExpect(jsonPath("$.items[0].accounts.disabled", is(0)))
                .andExpect(jsonPath("$.items[0].aliases.total", is(0)))
                .andExpect(jsonPath("$.items[0].aliases.healthy", is(0)))
                .andExpect(jsonPath("$.items[0].aliases.needsReview", is(0)))
                .andExpect(jsonPath("$.items[0].aliases.paused", is(0)))
                .andExpect(jsonPath("$.items[1].name", is("zulu-summary.example.com")))
                .andExpect(jsonPath("$.items[1].accounts.total", is(2)))
                .andExpect(jsonPath("$.items[1].accounts.enabled", is(1)))
                .andExpect(jsonPath("$.items[1].accounts.disabled", is(1)))
                .andExpect(jsonPath("$.items[1].aliases.total", is(3)))
                .andExpect(jsonPath("$.items[1].aliases.healthy", is(1)))
                .andExpect(jsonPath("$.items[1].aliases.needsReview", is(1)))
                .andExpect(jsonPath("$.items[1].aliases.paused", is(1)))
                .andExpect(content().string(not(containsString("alice"))))
                .andExpect(content().string(not(containsString("enabled@example.net"))))
                .andExpect(jsonPath("$.items[1].accounts.accountIds").doesNotExist())
                .andExpect(jsonPath("$.items[1].aliases.aliasIds").doesNotExist());
    }

    @Test
    void list_shouldAggregateOnlyDomainsOnRequestedPage() throws Exception {
        mockMvc.perform(get("/api/v1/domain-summaries?page=2&pageSize=1")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(2)))
                .andExpect(jsonPath("$.pageSize", is(1)))
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].name", is("zulu-summary.example.com")))
                .andExpect(jsonPath("$.items[0].accounts.total", is(2)))
                .andExpect(jsonPath("$.items[0].aliases.total", is(3)));
    }

    @Test
    void accountAndAliasLists_shouldPageWithinTheAuthorizedDomain() throws Exception {
        mockMvc.perform(get("/api/v1/domains/zulu-summary.example.com/accounts?page=2&pageSize=1")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(2)))
                .andExpect(jsonPath("$.pageSize", is(1)))
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].username", is("bob")));

        mockMvc.perform(get("/api/v1/domains/zulu-summary.example.com/aliases?page=2&pageSize=2")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(2)))
                .andExpect(jsonPath("$.pageSize", is(2)))
                .andExpect(jsonPath("$.total", is(5)))
                .andExpect(jsonPath("$.items", hasSize(2)));

        mockMvc.perform(get("/api/v1/domains/zulu-summary.example.com/aliases/mixed?page=2&pageSize=1")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].email", is("enabled@example.net")));
    }

    @Test
    void list_shouldFilterAccessibleDomainsBeforePagingAndCountSharedDomainsOnce() throws Exception {
        Domain zulu = domainRepository.findByName("zulu-summary.example.com").orElseThrow();
        Workspace firstWorkspace = saveWorkspace("summary-pagination-first", zulu);
        Workspace secondWorkspace = saveWorkspace("summary-pagination-second", zulu);
        User member = userRepository.save(new User("summary-pagination-member@example.com", "password"));
        User otherMember = userRepository.save(new User("z-pagination-member@example.com", "password"));
        Role memberRole = roleRepository.findByName(Role.ROLE_WORKSPACE_MEMBER).stream().findFirst().orElseThrow();
        Role workspaceAdminRole = roleRepository.findByName(Role.ROLE_WORKSPACE_ADMIN).stream()
                .findFirst().orElseThrow();
        saveMapping(member, firstWorkspace, workspaceAdminRole);
        saveMapping(member, secondWorkspace, memberRole);
        saveMapping(otherMember, firstWorkspace, memberRole);
        String memberAuthorization = "Bearer " + jwtTokenProvider.createToken(member.getEmail(), Collections.emptyList());

        mockMvc.perform(get("/api/v1/workspaces?page=1&pageSize=1")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.pageSize", is(1)))
                .andExpect(jsonPath("$.total", is((int) workspaceRepository.count())))
                .andExpect(jsonPath("$.items", hasSize(1)));

        mockMvc.perform(get("/api/v1/domain-summaries?page=1&pageSize=1")
                        .header("Authorization", memberAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.pageSize", is(1)))
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].name", is("zulu-summary.example.com")))
                .andExpect(jsonPath("$.items[0].accounts.total", is(2)))
                .andExpect(jsonPath("$.items[0].aliases.total", is(3)));

        mockMvc.perform(get("/api/v1/domain-summaries?page=2&pageSize=1")
                        .header("Authorization", memberAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.items", hasSize(0)));

        mockMvc.perform(get("/api/v1/domains?page=1&pageSize=1")
                        .header("Authorization", memberAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].name", is("zulu-summary.example.com")));

        mockMvc.perform(get("/api/v1/workspaces/accessible?page=2&pageSize=1")
                        .header("Authorization", memberAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(2)))
                .andExpect(jsonPath("$.pageSize", is(1)))
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].domainNames", hasSize(1)))
                .andExpect(jsonPath("$.items[0].domainNames[0]", is("zulu-summary.example.com")));

        mockMvc.perform(get("/api/v1/workspaces/managed?page=1&pageSize=1")
                        .header("Authorization", memberAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].name", is("summary-pagination-first")));

        mockMvc.perform(get("/api/v1/workspaces/" + firstWorkspace.getId() + "/mappings?page=2&pageSize=1")
                        .header("Authorization", memberAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(2)))
                .andExpect(jsonPath("$.pageSize", is(1)))
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].email", is("z-pagination-member@example.com")));
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

    private Workspace saveWorkspace(String name, Domain domain) {
        Workspace workspace = new Workspace();
        workspace.setName(name);
        workspace.setEnabled(true);
        workspace.setDomains(List.of(domain));
        return workspaceRepository.save(workspace);
    }

    private void saveMapping(User user, Workspace workspace, Role role) {
        WorkspaceMapping mapping = new WorkspaceMapping();
        mapping.setEnabled(true);
        mapping.setUser(user);
        mapping.setWorkspace(workspace);
        mapping.setRole(role);
        workspaceMappingRepository.save(mapping);
    }
}
