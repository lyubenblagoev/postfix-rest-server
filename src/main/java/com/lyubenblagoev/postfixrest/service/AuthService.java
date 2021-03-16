package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.service.model.AuthResponse;

public interface AuthService {

    AuthResponse createTokens(String email);

}
