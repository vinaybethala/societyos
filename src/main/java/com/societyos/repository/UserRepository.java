package com.societyos.repository;

import com.societyos.entity.User;
import com.societyos.entity.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.username = :login OR u.email = :login")
    Optional<User> findByUsernameOrEmail(@Param("login") String login);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"roles"})
    Page<User> findByRoles_Name(RoleType roleName, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"roles"})
    Page<User> findAll(Pageable pageable);
}

