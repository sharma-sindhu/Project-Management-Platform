package com.pm.platform.web;

import com.pm.platform.dto.CreateUserRequest;
import com.pm.platform.dto.UserDto;
import com.pm.platform.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserAccountService userAccountService;

    public UserApiController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping
    public UserDto createUser(@Valid @RequestBody CreateUserRequest body) {
        return userAccountService.createUser(body);
    }
}
