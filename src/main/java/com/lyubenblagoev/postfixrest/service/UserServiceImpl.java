package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.entity.Device;
import com.lyubenblagoev.postfixrest.entity.User;
import com.lyubenblagoev.postfixrest.repository.UserRepository;
import com.lyubenblagoev.postfixrest.service.model.DeviceInformation;
import com.lyubenblagoev.postfixrest.service.model.UserChangeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByEmailAndPassword(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return user;
        }
        boolean matches = passwordEncoder.matches(password, user.get().getPassword());
        if (matches) {
            return user;
        } else {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Optional<User> update(UserChangeRequest userChangeRequest) {
        Long id = Optional.ofNullable(userChangeRequest.getId()).orElse(-1L);
        User user = userRepository
                .findById(id)
                .orElse(new User(userChangeRequest.getEmail(), userChangeRequest.getPassword()));
        if (user.getId() != null) {
            if (!userChangeRequest.getEmail().equalsIgnoreCase(user.getEmail())) {
                Optional<User> existingLogin = userRepository.findByEmail(userChangeRequest.getEmail());
                if (existingLogin.isPresent()) {
                    throw new EntityExistsException("another user with that login already exists");
                }
            }
            user.setEmail(userChangeRequest.getEmail());
            if (userChangeRequest.getPassword().length() > 0) {
                user.setPassword(passwordEncoder.encode(userChangeRequest.getPassword()));
            }
        }
        User savedUser = userRepository.save(user);
        return Optional.of(savedUser);
    }

    @Override
    @Transactional
    public void addDeviceTokens(String email, DeviceInformation deviceInformation, String refreshToken,
                                Date refreshTokenExpirationDate) {
        User user = findByEmail(email)
                .orElseThrow(() -> {
                    logger.debug("No user " + email);
                    return new BadCredentialsException("Bad credentials");
                });
        Device device = new Device();
        device.setRefreshToken(refreshToken);
        device.setRefreshTokenExpirationDate(refreshTokenExpirationDate);
        device.setEnabled(true);
        device.setRemoteAddress(deviceInformation.getIpAddress());
        device.setType(deviceInformation.getBrowser());
        device.setOs(deviceInformation.getOs());
        user.addDevice(device);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateDeviceTokens(String email, DeviceInformation deviceInformation, String previousRefreshToken,
                                   String newRefreshToken, Date refreshTokenExpirationDate) {
        User user = findByEmail(email)
                .orElseThrow(() -> {
                    logger.debug("No user " + email);
                    return new BadCredentialsException("Bad credentials");
                });
        Optional<Device> deviceOptional = user.getDevices().stream()
                .filter(device -> device.isEnabled()
                        && device.getRefreshToken().equals(previousRefreshToken)
                        && device.getRefreshTokenExpirationDate().after(new Date()))
                .findFirst();
        if (deviceOptional.isEmpty()) {
            logger.debug("Invalid or expired refresh token " + previousRefreshToken);
            throw new BadCredentialsException("Bad credentials");
        }
        Device device = deviceOptional.get();
        device.setRefreshToken(newRefreshToken);
        device.setRefreshTokenExpirationDate(refreshTokenExpirationDate);
        device.setRemoteAddress(deviceInformation.getIpAddress());
        device.setType(deviceInformation.getBrowser());
        device.setOs(deviceInformation.getOs());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeDeviceToken(String email, String refreshToken) {
        User user = findByEmail(email)
                .orElseThrow(() -> {
                    logger.debug("No user " + email);
                    return new BadCredentialsException("Bad credentials");
                });
        Optional<Device> deviceOptional = user.getDevices().stream()
                .filter(device -> device.isEnabled()
                        && device.getRefreshToken().equals(refreshToken))
                .findFirst();
        if (deviceOptional.isEmpty()) {
            logger.debug("Invalid refresh token " + refreshToken);
            throw new BadCredentialsException("Bad credentials");
        }
        user.removeDevice(deviceOptional.get());
        userRepository.save(user);
    }

}
