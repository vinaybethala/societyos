package com.societyos.service;

import com.societyos.dto.response.UserResponse;
import com.societyos.entity.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse getUserById(Long id);
    Page<UserResponse> getAllUsers(RoleType role, Pageable pageable);
}

