package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.entity.User;
import com.lyubenblagoev.postfixrest.service.model.DeviceInformation;
import com.lyubenblagoev.postfixrest.service.model.UserChangeRequest;

import java.util.Date;
import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndPassword(String email, String password);

    Optional<User> update(UserChangeRequest userChangeRequest);

    void addDeviceTokens(String email, DeviceInformation device, String refreshToken, Date refreshTokenExpirationDate);

    void updateDeviceTokens(String email, DeviceInformation device, String previousRefreshToken,
            String newRefreshToken, Date refreshTokenExpirationDate);

    void removeDeviceToken(String email, String refreshToken);
}
