package com.lyubenblagoev.postfixrest.security;

import com.lyubenblagoev.postfixrest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsPasswordService implements UserDetailsPasswordService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsPasswordService.class);

    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    public CustomUserDetailsPasswordService(UserRepository userRepository,
                                            CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        return userRepository.findByEmail(user.getUsername())
                .map(u -> {
                    logger.debug("Upgrading password for user " + user.getUsername());
                    u.setPassword(newPassword);
                    userRepository.save(u);
                    return userDetailsService.loadUserByUsername(user.getUsername());
                })
                .orElseThrow(() -> new UsernameNotFoundException("No user found for " + user.getUsername()));
    }
}
