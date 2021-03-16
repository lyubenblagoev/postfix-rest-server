package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.entity.User;
import com.lyubenblagoev.postfixrest.security.JwtTokenProvider;
import com.lyubenblagoev.postfixrest.security.RefreshTokenProvider;
import com.lyubenblagoev.postfixrest.security.UserPrincipal;
import com.lyubenblagoev.postfixrest.service.model.AuthResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final UserService userService;

    public AuthServiceImpl(JwtTokenProvider jwtTokenProvider,
                           RefreshTokenProvider refreshTokenProvider,
                           UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenProvider = refreshTokenProvider;
        this.userService = userService;
    }

    @Override
    public AuthResponse createTokens(String email) {
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new EntityNotFoundException("Failed to find user with email " + email);
        }
        UserPrincipal userPrincipal = new UserPrincipal(userOptional.get());
        String token = jwtTokenProvider.createToken(userPrincipal.getUsername(), userPrincipal.getAuthorities());
        RefreshTokenProvider.RefreshToken refreshToken = refreshTokenProvider.createToken();
        return new AuthResponse(token, refreshToken.getToken(), refreshToken.getExpirationDate());
    }

}
