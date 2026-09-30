package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.BadRequestException;
import com.lyubenblagoev.postfixrest.NotFoundException;
import com.lyubenblagoev.postfixrest.controller.model.InvitationRequest;
import com.lyubenblagoev.postfixrest.controller.model.InvitationRole;
import com.lyubenblagoev.postfixrest.entity.Role;
import com.lyubenblagoev.postfixrest.entity.User;
import com.lyubenblagoev.postfixrest.entity.Workspace;
import com.lyubenblagoev.postfixrest.entity.WorkspaceInvitation;
import com.lyubenblagoev.postfixrest.entity.WorkspaceMapping;
import com.lyubenblagoev.postfixrest.repository.RoleRepository;
import com.lyubenblagoev.postfixrest.repository.UserRepository;
import com.lyubenblagoev.postfixrest.repository.WorkspaceInvitationRepository;
import com.lyubenblagoev.postfixrest.repository.WorkspaceMappingRepository;
import com.lyubenblagoev.postfixrest.repository.WorkspaceRepository;
import com.lyubenblagoev.postfixrest.service.model.InvitationResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class InvitationService {

    private final WorkspaceInvitationRepository invitationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMappingRepository mappingRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${users.invitation.validity-hours:72}")
    private long validityHours;

    public InvitationService(WorkspaceInvitationRepository invitationRepository,
                            WorkspaceRepository workspaceRepository,
                            WorkspaceMappingRepository mappingRepository,
                            UserRepository userRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.invitationRepository = invitationRepository;
        this.workspaceRepository = workspaceRepository;
        this.mappingRepository = mappingRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public InvitationResource create(InvitationRequest request) {
        if (request.getRole() == null) {
            throw new BadRequestException("role is required");
        }
        Workspace workspace = null;
        String roleName;
        if (request.getRole() == InvitationRole.ADMIN) {
            if (request.getWorkspaceId() != null) {
                throw new BadRequestException("An admin invitation cannot select a workspace");
            }
            roleName = Role.ROLE_ADMIN;
        } else {
            if (request.getWorkspaceId() == null) {
                throw new BadRequestException("A workspace is required for a workspace invitation");
            }
            workspace = workspaceRepository.findById(request.getWorkspaceId())
                    .orElseThrow(() -> new NotFoundException("Workspace not found"));
            roleName = request.getRole() == InvitationRole.WORKSPACE_ADMIN
                    ? Role.ROLE_WORKSPACE_ADMIN : Role.ROLE_WORKSPACE_MEMBER;
        }
        Role role = roleRepository.findByName(roleName).stream().findFirst()
                .orElseThrow(() -> new NotFoundException("Role " + roleName + " not found"));
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        byte[] secret = new byte[32];
        secureRandom.nextBytes(secret);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        Date expiresAt = Date.from(Instant.now().plusSeconds(Math.multiplyExact(validityHours, 3600)));

        WorkspaceInvitation invitation = new WorkspaceInvitation();
        invitation.setEnabled(true);
        invitation.setEmail(email);
        invitation.setTokenHash(hash(token));
        invitation.setRole(role);
        invitation.setWorkspace(workspace);
        invitation.setExpiresAt(expiresAt);
        invitationRepository.save(invitation);
        return new InvitationResource(email, roleName, workspace == null ? null : workspace.getId(), token, expiresAt);
    }

    @Transactional
    public void accept(String token, String password) {
        WorkspaceInvitation invitation = invitationRepository.findByTokenHashForUpdate(hash(token))
                .orElseThrow(() -> new BadRequestException("Invitation is invalid or expired"));
        Date now = new Date();
        if (invitation.getAcceptedAt() != null || !invitation.getExpiresAt().after(now)) {
            throw new BadRequestException("Invitation is invalid or expired");
        }

        User user = userRepository.findByEmailIgnoreCase(invitation.getEmail())
                .orElseGet(() -> new User(invitation.getEmail(), ""));
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        if (invitation.getWorkspace() == null) {
            if (user.getRoles() == null) {
                user.setRoles(new ArrayList<>());
            }
            boolean alreadyHasRole = user.getRoles().stream()
                    .anyMatch(role -> role.getId().equals(invitation.getRole().getId()));
            if (!alreadyHasRole) {
                user.getRoles().add(invitation.getRole());
            }
            userRepository.save(user);
        } else {
            User savedUser = userRepository.save(user);
            Workspace workspace = invitation.getWorkspace();
            Optional<WorkspaceMapping> existingMapping = mappingRepository.findByUserIdAndWorkspaceId(
                    savedUser.getId(), workspace.getId());
            WorkspaceMapping mapping = existingMapping.orElseGet(WorkspaceMapping::new);
            boolean membershipChanged = existingMapping.isEmpty() || !mapping.isEnabled()
                    || !mapping.getRole().getId().equals(invitation.getRole().getId());
            mapping.setEnabled(true);
            mapping.setUser(savedUser);
            mapping.setWorkspace(workspace);
            mapping.setRole(invitation.getRole());
            mappingRepository.save(mapping);
            if (membershipChanged) {
                workspace.setUpdated(now);
                workspaceRepository.save(workspace);
            }
        }
        invitation.setAcceptedAt(now);
        invitationRepository.save(invitation);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
