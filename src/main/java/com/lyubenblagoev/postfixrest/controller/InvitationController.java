package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.BadRequestException;
import com.lyubenblagoev.postfixrest.ForbiddenException;
import com.lyubenblagoev.postfixrest.controller.model.InvitationRole;
import com.lyubenblagoev.postfixrest.service.InvitationService;
import com.lyubenblagoev.postfixrest.service.WorkspaceAccessService;
import com.lyubenblagoev.postfixrest.controller.model.InvitationRequest;
import com.lyubenblagoev.postfixrest.service.model.InvitationResource;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invitations")
public class InvitationController {

    private final InvitationService invitationService;
    private final WorkspaceAccessService accessService;

    public InvitationController(InvitationService invitationService, WorkspaceAccessService accessService) {
        this.invitationService = invitationService;
        this.accessService = accessService;
    }

    @PostMapping
    public ResponseEntity<InvitationResource> create(@Valid @RequestBody InvitationRequest request) {
        if (!accessService.isAdmin()) {
            if (request.getRole() == null) {
                throw new BadRequestException("role is required");
            }
            if (request.getRole() != InvitationRole.WORKSPACE_MEMBER) {
                throw new ForbiddenException("Workspace administrators can invite workspace members only");
            }
            if (request.getWorkspaceId() == null) {
                throw new BadRequestException("A workspace is required for a workspace member invitation");
            }
            accessService.requireWorkspaceAdmin(request.getWorkspaceId());
            if (accessService.currentUserEmail().equalsIgnoreCase(request.getEmail().trim())) {
                throw new ForbiddenException(
                        "Another workspace administrator or global administrator must invite you as a member");
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(invitationService.create(request));
    }
}
