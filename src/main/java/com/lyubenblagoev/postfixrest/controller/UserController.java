package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.NotFoundException;
import com.lyubenblagoev.postfixrest.controller.model.UserUpdateRequest;
import com.lyubenblagoev.postfixrest.service.UserService;
import com.lyubenblagoev.postfixrest.service.model.UserChangeRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final String INVALID_USER_CREDENTIALS_MESSAGE = "Invalid user credentials";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{login:.+}")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdateRequest userUpdateRequest,
                                        @PathVariable("login") String currentLogin) {
        String currentPassword = userUpdateRequest.getOldPassword();
        UserChangeRequest userChangeRequest = new UserChangeRequest(
                userUpdateRequest.getEmail(),
                userUpdateRequest.getPassword(),
                userUpdateRequest.getPasswordConfirmation());
        return userService.findByEmailAndPassword(currentLogin, currentPassword)
                .map(u -> {
                    userChangeRequest.setId(u.getId());
                    userService.update(userChangeRequest);
                    return ResponseEntity.ok().build();
                })
                .orElseThrow(() -> new NotFoundException(INVALID_USER_CREDENTIALS_MESSAGE));
    }
}
