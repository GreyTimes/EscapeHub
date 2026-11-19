package com.escapehub.escapehub.user.repository;

import com.escapehub.escapehub.user.entity.User;
import com.escapehub.escapehub.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByRole(UserRole role);
}