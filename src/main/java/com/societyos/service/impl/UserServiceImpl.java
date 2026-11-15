package com.societyos.service.impl;

import com.societyos.dto.response.UserResponse;
import com.societyos.entity.User;
import com.societyos.entity.enums.RoleType;
import com.societyos.exception.ResourceNotFoundException;
import com.societyos.repository.UserRepository;
import com.societyos.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(RoleType role, Pageable pageable) {
        Page<User> page;
        if (role != null) {
            page = userRepository.findByRoles_Name(role, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }
        return page.map(UserResponse::fromEntity);
    }
}
