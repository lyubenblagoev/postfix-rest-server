package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.ForbiddenException;
import com.lyubenblagoev.postfixrest.NotFoundException;
import com.lyubenblagoev.postfixrest.UnauthorizedException;
import com.lyubenblagoev.postfixrest.entity.Domain;
import com.lyubenblagoev.postfixrest.entity.Role;
import com.lyubenblagoev.postfixrest.entity.WorkspaceMapping;
import com.lyubenblagoev.postfixrest.repository.DomainRepository;
import com.lyubenblagoev.postfixrest.repository.AccountRepository;
import com.lyubenblagoev.postfixrest.repository.WorkspaceMappingRepository;
import com.lyubenblagoev.postfixrest.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WorkspaceAccessService {

    private final DomainRepository domainRepository;
    private final AccountRepository accountRepository;
    private final WorkspaceMappingRepository mappingRepository;

    public WorkspaceAccessService(DomainRepository domainRepository,
                                  AccountRepository accountRepository,
                                  WorkspaceMappingRepository mappingRepository) {
        this.domainRepository = domainRepository;
        this.accountRepository = accountRepository;
        this.mappingRepository = mappingRepository;
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(("ROLE_" + Role.ROLE_ADMIN)::equals);
    }

    public Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new UnauthorizedException("Authentication required");
    }

    public String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUsername();
        }
        throw new UnauthorizedException("Authentication required");
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new ForbiddenException("Administrator role required");
        }
    }

    public void requireWorkspaceAdmin(Long workspaceId) {
        if (isAdmin()) {
            return;
        }
        WorkspaceMapping mapping = mappingRepository.findEnabledByUserIdAndWorkspaceId(currentUserId(), workspaceId)
                .orElseThrow(() -> new UnauthorizedException("Workspace access denied"));
        if (!Role.ROLE_WORKSPACE_ADMIN.equals(mapping.getRole().getName())) {
            throw new ForbiddenException("Workspace administrator role required");
        }
    }

    public void requireWorkspaceAccess(Long workspaceId) {
        if (!isAdmin() && mappingRepository.findEnabledByUserIdAndWorkspaceId(currentUserId(), workspaceId).isEmpty()) {
            throw new UnauthorizedException("Workspace access denied");
        }
    }

    public void assertDomainNameAccess(String domainName) {
        if (isAdmin()) {
            return;
        }
        Domain domain = domainRepository.findByName(domainName)
                .orElseThrow(() -> new NotFoundException("Domain " + domainName + " not found"));
        assertDomainIdAccess(domain.getId());
    }

    public void assertDomainIdAccess(Long domainId) {
        if (isAdmin()) {
            return;
        }
        if (!domainRepository.existsById(domainId)) {
            throw new NotFoundException("Domain not found");
        }
        if (!mappingRepository.hasDomainAccess(currentUserId(), domainId)) {
            throw new UnauthorizedException("Domain access denied");
        }
    }

    public void assertAccountAccess(Long accountId) {
        if (isAdmin()) {
            return;
        }
        Long domainId = accountRepository.findDomainIdById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        assertDomainIdAccess(domainId);
    }

    /** Returns null for an administrator, whose domain set is unrestricted. */
    public List<Long> accessibleDomainIds() {
        if (isAdmin()) {
            return null;
        }
        return mappingRepository.findAccessibleDomainIds(currentUserId());
    }
}
