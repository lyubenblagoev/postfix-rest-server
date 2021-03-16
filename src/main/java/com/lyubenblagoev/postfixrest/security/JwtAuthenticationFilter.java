package com.lyubenblagoev.postfixrest.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
    private final JwtAuthenticationProvider authenticationProvider;

    public JwtAuthenticationFilter(JwtAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication authRequest = authenticationConverter.convert(request);
            if (authRequest == null || !(authRequest instanceof JwtAuthenticationToken)) {
                logger.trace("Did not process authentication request since failed to find "
                        + "JWT in Bearer Authorization header");
                filterChain.doFilter(request, response);
                return;
            }
            String token = ((JwtAuthenticationToken) authRequest).getToken();
            if (token != null) {
                Authentication authentication = authenticationProvider.authenticate(authRequest);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            logger.debug("Failed to process authentication request: " + e.getMessage());
            response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
            return;
        }
        filterChain.doFilter(request, response);
    }

}
