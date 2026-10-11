package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.BadRequestException;
import com.lyubenblagoev.postfixrest.NotFoundException;
import com.lyubenblagoev.postfixrest.entity.Domain;
import com.lyubenblagoev.postfixrest.entity.Role;
import com.lyubenblagoev.postfixrest.entity.User;
import com.lyubenblagoev.postfixrest.entity.Workspace;
import com.lyubenblagoev.postfixrest.entity.WorkspaceMapping;
import com.lyubenblagoev.postfixrest.repository.DomainRepository;
import com.lyubenblagoev.postfixrest.repository.RoleRepository;
import com.lyubenblagoev.postfixrest.repository.UserRepository;
import com.lyubenblagoev.postfixrest.repository.WorkspaceInvitationRepository;
import com.lyubenblagoev.postfixrest.repository.WorkspaceMappingRepository;
import com.lyubenblagoev.postfixrest.repository.WorkspaceRepository;
import com.lyubenblagoev.postfixrest.service.model.WorkspaceChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.WorkspaceMappingRequest;
import com.lyubenblagoev.postfixrest.service.model.WorkspaceMappingResource;
import com.lyubenblagoev.postfixrest.service.model.WorkspaceResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMappingRepository mappingRepository;
    private final WorkspaceInvitationRepository invitationRepository;
    private final DomainRepository domainRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                            WorkspaceMappingRepository mappingRepository,
                            WorkspaceInvitationRepository invitationRepository,
                            DomainRepository domainRepository,
                            UserRepository userRepository,
                            RoleRepository roleRepository) {
        this.workspaceRepository = workspaceRepository;
        this.mappingRepository = mappingRepository;
        this.invitationRepository = invitationRepository;
        this.domainRepository = domainRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<WorkspaceResource> findAll() {
        return workspaceRepository.findAll(Sort.by("name")).stream()
                .map(WorkspaceResource::fromWorkspace).toList();
    }

    public Page<WorkspaceResource> findAll(Pageable pageable) {
        return toWorkspaceResourcePage(workspaceRepository.findAll(pageable));
    }

    public List<WorkspaceResource> findManagedByUser(Long userId) {
        return mappingRepository.findEnabledWorkspacesByUserIdAndRoleName(userId, Role.ROLE_WORKSPACE_ADMIN)
                .stream().map(WorkspaceResource::fromWorkspace).toList();
    }

    public Page<WorkspaceResource> findManagedByUser(Long userId, Pageable pageable) {
        return toWorkspaceResourcePage(mappingRepository.findPageOfEnabledWorkspacesByUserIdAndRoleName(
                userId, Role.ROLE_WORKSPACE_ADMIN, pageable));
    }

    public List<WorkspaceResource> findAccessibleByUser(Long userId) {
        return mappingRepository.findEnabledWorkspacesByUserId(userId).stream()
                .map(WorkspaceResource::fromWorkspace).toList();
    }

    public Page<WorkspaceResource> findAccessibleByUser(Long userId, Pageable pageable) {
        return toWorkspaceResourcePage(mappingRepository.findPageOfEnabledWorkspacesByUserId(userId, pageable));
    }

    public WorkspaceResource findById(Long id) {
        return WorkspaceResource.fromWorkspace(getWorkspace(id));
    }

    @Transactional
    public WorkspaceResource create(WorkspaceChangeRequest request) {
        if (workspaceRepository.findByName(request.getName()).isPresent()) {
            throw new EntityExistsException("Workspace already exists");
        }
        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.setEnabled(true);
        if (request.getDomainNames() != null) {
            workspace.setDomains(findDomains(request.getDomainNames()));
        }
        return WorkspaceResource.fromWorkspace(workspaceRepository.save(workspace));
    }

    @Transactional
    public WorkspaceResource update(Long id, WorkspaceChangeRequest request) {
        Workspace workspace = getWorkspace(id);
        if (!workspace.getName().equals(request.getName())
                && workspaceRepository.findByName(request.getName()).isPresent()) {
            throw new EntityExistsException("Workspace already exists");
        }
        boolean changed = !workspace.getName().equals(request.getName());
        workspace.setName(request.getName());
        if (request.getDomainNames() != null) {
            List<Domain> domains = findDomains(request.getDomainNames());
            changed |= !sameDomains(workspace.getDomains(), domains);
            workspace.setDomains(domains);
        }
        if (changed) {
            markUpdated(workspace);
        }
        return WorkspaceResource.fromWorkspace(workspaceRepository.save(workspace));
    }

    @Transactional
    public void delete(Long id) {
        Workspace workspace = getWorkspace(id);
        invitationRepository.deleteByWorkspaceId(id);
        workspaceRepository.delete(workspace);
    }

    @Transactional
    public WorkspaceResource replaceDomains(Long workspaceId, List<String> domainNames) {
        Workspace workspace = getWorkspace(workspaceId);
        List<Domain> domains = findDomains(domainNames == null ? List.of() : domainNames);
        if (!sameDomains(workspace.getDomains(), domains)) {
            workspace.setDomains(domains);
            markUpdated(workspace);
        }
        return WorkspaceResource.fromWorkspace(workspaceRepository.save(workspace));
    }

    @Transactional
    public WorkspaceResource addDomain(Long workspaceId, String domainName) {
        Workspace workspace = getWorkspace(workspaceId);
        Domain domain = domainRepository.findByName(domainName)
                .orElseThrow(() -> new NotFoundException("Domain " + domainName + " not found"));
        if (workspace.getDomains().stream().noneMatch(existing -> existing.getId().equals(domain.getId()))) {
            workspace.getDomains().add(domain);
            markUpdated(workspace);
        }
        return WorkspaceResource.fromWorkspace(workspaceRepository.save(workspace));
    }

    @Transactional
    public WorkspaceResource removeDomain(Long workspaceId, String domainName) {
        Workspace workspace = getWorkspace(workspaceId);
        if (workspace.getDomains().removeIf(domain -> domain.getName().equals(domainName))) {
            markUpdated(workspace);
        }
        return WorkspaceResource.fromWorkspace(workspaceRepository.save(workspace));
    }

    public List<WorkspaceMappingResource> getMappings(Long workspaceId) {
        getWorkspace(workspaceId);
        return mappingRepository.findByWorkspaceIdWithUser(workspaceId).stream()
                .map(WorkspaceMappingResource::fromMapping).toList();
    }

    public Page<WorkspaceMappingResource> getMappings(Long workspaceId, Pageable pageable) {
        getWorkspace(workspaceId);
        return mappingRepository.findPageByWorkspaceIdWithUser(workspaceId, pageable)
                .map(WorkspaceMappingResource::fromMapping);
    }

    @Transactional
    public WorkspaceMappingResource saveMapping(Long workspaceId, WorkspaceMappingRequest request) {
        Workspace workspace = getWorkspace(workspaceId);
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User " + request.getEmail() + " not found"));
        String roleName = request.getRole() == null || request.getRole().isBlank()
                ? Role.ROLE_WORKSPACE_MEMBER : request.getRole();
        if (Role.ROLE_ADMIN.equals(roleName)) {
            throw new BadRequestException("ADMIN is a global role and cannot be assigned through a workspace mapping");
        }
        Role role = roleRepository.findByName(roleName).stream().findFirst()
                .orElseThrow(() -> new NotFoundException("Role " + roleName + " not found"));
        WorkspaceMapping mapping = mappingRepository.findByUserIdAndWorkspaceId(user.getId(), workspaceId)
                .orElseGet(WorkspaceMapping::new);
        boolean changed = !mapping.isEnabled() || mapping.getRole() == null
                || !roleName.equals(mapping.getRole().getName());
        mapping.setEnabled(true);
        mapping.setUser(user);
        mapping.setWorkspace(workspace);
        mapping.setRole(role);
        WorkspaceMappingResource resource = WorkspaceMappingResource.fromMapping(mappingRepository.save(mapping));
        if (changed) {
            markUpdated(workspace);
            workspaceRepository.save(workspace);
        }
        return resource;
    }

    @Transactional
    public void deleteMapping(Long workspaceId, String email) {
        Workspace workspace = getWorkspace(workspaceId);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User " + email + " not found"));
        WorkspaceMapping mapping = mappingRepository.findByUserIdAndWorkspaceId(user.getId(), workspaceId)
                .orElseThrow(() -> new NotFoundException("Workspace mapping not found"));
        mappingRepository.delete(mapping);
        markUpdated(workspace);
        workspaceRepository.save(workspace);
    }

    Workspace getWorkspace(Long id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Workspace not found"));
    }

    private Page<WorkspaceResource> toWorkspaceResourcePage(Page<Workspace> page) {
        List<Long> workspaceIds = page.getContent().stream().map(Workspace::getId).toList();
        if (workspaceIds.isEmpty()) {
            return page.map(WorkspaceResource::fromWorkspace);
        }
        Map<Long, Workspace> workspacesById = workspaceRepository.findAllWithDomainsByIdIn(workspaceIds).stream()
                .collect(Collectors.toMap(Workspace::getId, Function.identity()));
        List<WorkspaceResource> resources = workspaceIds.stream()
                .map(workspacesById::get)
                .map(WorkspaceResource::fromWorkspace)
                .toList();
        return new PageImpl<>(resources, page.getPageable(), page.getTotalElements());
    }

    private List<Domain> findDomains(List<String> names) {
        LinkedHashSet<String> uniqueNames = new LinkedHashSet<>(names);
        if (uniqueNames.size() != names.size()) {
            throw new BadRequestException("Domain names must not contain duplicates");
        }
        List<Domain> domains = new ArrayList<>();
        for (String name : uniqueNames) {
            domains.add(domainRepository.findByName(name)
                    .orElseThrow(() -> new NotFoundException("Domain " + name + " not found")));
        }
        return domains;
    }

    private boolean sameDomains(List<Domain> first, List<Domain> second) {
        Set<Long> firstIds = first.stream().map(Domain::getId).collect(Collectors.toSet());
        Set<Long> secondIds = second.stream().map(Domain::getId).collect(Collectors.toSet());
        return firstIds.equals(secondIds);
    }

    private void markUpdated(Workspace workspace) {
        workspace.setUpdated(new Date());
    }
}
