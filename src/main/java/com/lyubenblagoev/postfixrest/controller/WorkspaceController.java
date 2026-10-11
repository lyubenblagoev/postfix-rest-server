package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.ForbiddenException;
import com.lyubenblagoev.postfixrest.configuration.PaginationProperties;
import com.lyubenblagoev.postfixrest.entity.Role;
import com.lyubenblagoev.postfixrest.service.WorkspaceAccessService;
import com.lyubenblagoev.postfixrest.service.WorkspaceService;
import com.lyubenblagoev.postfixrest.service.model.WorkspaceChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.WorkspaceDomainRequest;
import com.lyubenblagoev.postfixrest.service.model.WorkspaceDomainsRequest;
import com.lyubenblagoev.postfixrest.service.model.WorkspaceMappingRequest;
import com.lyubenblagoev.postfixrest.service.model.WorkspaceMappingResource;
import com.lyubenblagoev.postfixrest.service.model.WorkspaceResource;
import com.lyubenblagoev.postfixrest.service.model.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final WorkspaceAccessService accessService;
    private final PaginationProperties paginationProperties;

    public WorkspaceController(WorkspaceService workspaceService, WorkspaceAccessService accessService,
                               PaginationProperties paginationProperties) {
        this.workspaceService = workspaceService;
        this.accessService = accessService;
        this.paginationProperties = paginationProperties;
    }

    @GetMapping
    public PagedResponse<WorkspaceResource> list(@RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer pageSize) {
        accessService.requireAdmin();
        Pageable pageable = paginationProperties.toPageable(page, pageSize, Sort.by("name", "id"));
        return PagedResponse.from(workspaceService.findAll(pageable));
    }

    @GetMapping("/managed")
    public PagedResponse<WorkspaceResource> managed(@RequestParam(required = false) Integer page,
                                                    @RequestParam(required = false) Integer pageSize) {
        if (accessService.isAdmin()) {
            Pageable pageable = paginationProperties.toPageable(page, pageSize, Sort.by("name", "id"));
            return PagedResponse.from(workspaceService.findAll(pageable));
        }
        Pageable pageable = paginationProperties.toPageable(page, pageSize, Sort.unsorted());
        return PagedResponse.from(workspaceService.findManagedByUser(accessService.currentUserId(), pageable));
    }

    @GetMapping("/accessible")
    public PagedResponse<WorkspaceResource> accessible(@RequestParam(required = false) Integer page,
                                                       @RequestParam(required = false) Integer pageSize) {
        if (accessService.isAdmin()) {
            Pageable pageable = paginationProperties.toPageable(page, pageSize, Sort.by("name", "id"));
            return PagedResponse.from(workspaceService.findAll(pageable));
        }
        Pageable pageable = paginationProperties.toPageable(page, pageSize, Sort.unsorted());
        return PagedResponse.from(workspaceService.findAccessibleByUser(accessService.currentUserId(), pageable));
    }

    @PostMapping
    public ResponseEntity<WorkspaceResource> create(@Valid @RequestBody WorkspaceChangeRequest request) {
        accessService.requireAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.create(request));
    }

    @GetMapping("/{workspaceId:[0-9]+}")
    public WorkspaceResource get(@PathVariable Long workspaceId) {
        accessService.requireWorkspaceAccess(workspaceId);
        return workspaceService.findById(workspaceId);
    }

    @PutMapping("/{workspaceId}")
    public WorkspaceResource update(@PathVariable Long workspaceId,
                                    @Valid @RequestBody WorkspaceChangeRequest request) {
        accessService.requireAdmin();
        return workspaceService.update(workspaceId, request);
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> delete(@PathVariable Long workspaceId) {
        accessService.requireAdmin();
        workspaceService.delete(workspaceId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{workspaceId}/domains")
    public WorkspaceResource replaceDomains(@PathVariable Long workspaceId,
                                            @RequestBody WorkspaceDomainsRequest request) {
        accessService.requireAdmin();
        return workspaceService.replaceDomains(workspaceId, request.getDomainNames());
    }

    @PostMapping("/{workspaceId}/domains")
    public WorkspaceResource addDomain(@PathVariable Long workspaceId,
                                       @Valid @RequestBody WorkspaceDomainRequest request) {
        accessService.requireAdmin();
        return workspaceService.addDomain(workspaceId, request.getDomainName());
    }

    @DeleteMapping("/{workspaceId}/domains/{domainName:.+}")
    public WorkspaceResource removeDomain(@PathVariable Long workspaceId, @PathVariable String domainName) {
        accessService.requireAdmin();
        return workspaceService.removeDomain(workspaceId, domainName);
    }

    @GetMapping("/{workspaceId}/mappings")
    public PagedResponse<WorkspaceMappingResource> mappings(@PathVariable Long workspaceId,
                                                            @RequestParam(required = false) Integer page,
                                                            @RequestParam(required = false) Integer pageSize) {
        accessService.requireWorkspaceAdmin(workspaceId);
        Pageable pageable = paginationProperties.toPageable(page, pageSize, Sort.unsorted());
        return PagedResponse.from(workspaceService.getMappings(workspaceId, pageable));
    }

    @PostMapping("/{workspaceId}/mappings")
    public WorkspaceMappingResource addMapping(@PathVariable Long workspaceId,
                                               @Valid @RequestBody WorkspaceMappingRequest request) {
        accessService.requireWorkspaceAdmin(workspaceId);
        String roleName = request.getRole() == null || request.getRole().isBlank()
                ? Role.ROLE_WORKSPACE_MEMBER : request.getRole();
        if (!Role.ROLE_WORKSPACE_ADMIN.equals(roleName)) {
            preventSelfWorkspaceAdminAccessLoss(request.getEmail());
        }
        return workspaceService.saveMapping(workspaceId, request);
    }

    @DeleteMapping("/{workspaceId}/mappings/{email:.+}")
    public ResponseEntity<Void> deleteMapping(@PathVariable Long workspaceId, @PathVariable String email) {
        accessService.requireWorkspaceAdmin(workspaceId);
        preventSelfWorkspaceAdminAccessLoss(email);
        workspaceService.deleteMapping(workspaceId, email);
        return ResponseEntity.noContent().build();
    }

    private void preventSelfWorkspaceAdminAccessLoss(String email) {
        if (!accessService.isAdmin() && accessService.currentUserEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException(
                    "Another workspace administrator or global administrator must change your mapping");
        }
    }
}
