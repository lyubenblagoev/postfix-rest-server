package com.lyubenblagoev.postfixrest.security;

import com.lyubenblagoev.postfixrest.entity.Role;
import com.lyubenblagoev.postfixrest.entity.User;
import com.lyubenblagoev.postfixrest.repository.RoleRepository;
import com.lyubenblagoev.postfixrest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DefaultAdminUserInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAdminUserInitializer.class);

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Value("${users.default-user.email}")
    private String defaultUserEmail;

    @Value("${users.default-user.password}")
    private String defaultUserPassword;

    public DefaultAdminUserInitializer(PasswordEncoder passwordEncoder,
                                       UserRepository userRepository,
                                       RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void run() {
        createDefaultRolesIfNotExists();
        createDefaultUserIfNotExists();
    }

    private void createDefaultRolesIfNotExists() {
        createRoleIfNotExists(Role.ROLE_ADMIN);
        createRoleIfNotExists(Role.ROLE_WORKSPACE_ADMIN);
        createRoleIfNotExists(Role.ROLE_WORKSPACE_MEMBER);
    }

    private void createRoleIfNotExists(String roleName) {
        if (!roleRepository.findByName(roleName).isEmpty()) {
            return;
        }
        Role role = new Role();
        role.setName(roleName);
        role.setEnabled(true);
        roleRepository.save(role);
        logger.info("Created default role \"{}\"", roleName);
    }

    private void createDefaultUserIfNotExists() {
        if (userRepository.findAll().isEmpty()) {
            User adminUser = new User(defaultUserEmail, passwordEncoder.encode(defaultUserPassword));
            adminUser.setRoles(roleRepository.findByName(Role.ROLE_ADMIN));
            userRepository.save(adminUser);
            logger.info("Created default admin user with username \"{}\" and password \"{}\"",
                    defaultUserEmail, defaultUserPassword);
        }
    }
}
