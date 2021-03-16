package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.controller.model.LogoutRequest;
import com.lyubenblagoev.postfixrest.service.model.DeviceInformation;
import com.lyubenblagoev.postfixrest.service.AuthService;
import com.lyubenblagoev.postfixrest.service.UserService;
import com.lyubenblagoev.postfixrest.service.model.AuthResponse;
import com.lyubenblagoev.postfixrest.service.model.LoginRequest;
import com.lyubenblagoev.postfixrest.service.model.RefreshTokenRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationProvider authenticationProvider;
    private final AuthService authService;
    private final UserService userService;
    private final DeviceInformationConverter deviceInformationConverter;

    public AuthController(DaoAuthenticationProvider authenticationProvider,
                          AuthService authService,
                          UserService userService,
                          DeviceInformationConverter deviceInformationConverter) {
        this.authenticationProvider = authenticationProvider;
        this.authService = authService;
        this.userService = userService;
        this.deviceInformationConverter = deviceInformationConverter;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword()));
        DeviceInformation device = deviceInformationConverter.convert(request);
        logger.debug("New login for user {} from IP address {} using {} on {}", loginRequest.getLogin(),
                device.getIpAddress(), device.getBrowser(), device.getOs());
        AuthResponse newTokens = authService.createTokens(loginRequest.getLogin());
        userService.addDeviceTokens(loginRequest.getLogin(), device,
                newTokens.getRefreshToken(), newTokens.getRefreshTokenExpirationDate());
        return ResponseEntity.ok(newTokens);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest logoutRequest, Authentication authentication) {
        if (authentication == null) {
            logger.warn("Unauthenticated logout request from " + logoutRequest.getLogin());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        userService.removeDeviceToken(logoutRequest.getLogin(), logoutRequest.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest, HttpServletRequest request) {
        DeviceInformation device = deviceInformationConverter.convert(request);
        logger.debug("New refresh token request for user {} from IP address {} using {} on {}",
                refreshTokenRequest.getLogin(), device.getIpAddress(), device.getBrowser(), device.getOs());
        AuthResponse newTokens = authService.createTokens(refreshTokenRequest.getLogin());
        userService.updateDeviceTokens(refreshTokenRequest.getLogin(), device, refreshTokenRequest.getRefreshToken(),
                newTokens.getRefreshToken(), newTokens.getRefreshTokenExpirationDate());
        return ResponseEntity.ok(newTokens);
    }
}
