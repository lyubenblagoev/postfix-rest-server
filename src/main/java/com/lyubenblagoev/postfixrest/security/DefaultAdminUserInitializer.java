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

import javax.annotation.PostConstruct;

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
        if (roleRepository.findAll().isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(Role.ROLE_ADMIN);
            adminRole.setEnabled(true);
            roleRepository.save(adminRole);
            logger.info("Created default admin role \"{}\"", Role.ROLE_ADMIN);
        }
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
