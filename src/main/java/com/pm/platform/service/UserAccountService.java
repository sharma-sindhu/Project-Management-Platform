package com.pm.platform.service;

import com.pm.platform.domain.UserEntity;
import com.pm.platform.dto.CreateUserRequest;
import com.pm.platform.dto.UserDto;
import com.pm.platform.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {

    private final UserRepository userRepository;

    public UserAccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDto createUser(CreateUserRequest req) {
        if (userRepository.findByEmailIgnoreCase(req.email().trim()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        String id =
                req.user_id() != null && !req.user_id().isBlank()
                        ? req.user_id().trim()
                        : UUID.randomUUID().toString();
        if (userRepository.existsById(id)) {
            throw new IllegalArgumentException("user_id already exists: " + id);
        }
        UserEntity u =
                new UserEntity(id, req.email().trim(), req.display_name().trim());
        userRepository.save(u);
        return new UserDto(u.getId(), u.getEmail(), u.getDisplayName());
    }
}
