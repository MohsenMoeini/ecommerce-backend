package com.ecommerce.service.interfaces;

import com.ecommerce.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user);
    User updateUser(Long userId, User user);
    Optional<User> getUserById(Long userId);
    Optional<User> getUserByEmail(String email);
    List<User> getAllUsers();
    void deactivateUser(Long userId);
    void activateUser(Long userId);
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    void deleteUser(Long userId);
}
