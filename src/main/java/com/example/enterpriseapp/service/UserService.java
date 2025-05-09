package com.example.enterpriseapp.service;

import com.example.enterpriseapp.common.ApiResponse;
import com.example.enterpriseapp.dto.ChangePasswordRequest;
import com.example.enterpriseapp.dto.AdminChangePasswordRequest;
import com.example.enterpriseapp.dto.ChangeEmailRequest;
import com.example.enterpriseapp.dto.RegisterRequest;
import com.example.enterpriseapp.dto.UserInfoResponse;
import com.example.enterpriseapp.entity.Role;
import com.example.enterpriseapp.entity.User;
import com.example.enterpriseapp.entity.LogLevel;
import com.example.enterpriseapp.exception.CustomException;
import com.example.enterpriseapp.repository.RoleRepository;
import com.example.enterpriseapp.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final HttpServletRequest httpRequest;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, AuditService auditService,
                       HttpServletRequest httpRequest) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.httpRequest = httpRequest;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("User already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(404, "User not found"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(404, "User not found"));
    }

    public UserInfoResponse getCurrentUserInfo(String username) {
        User user = findByUsername(username);
        auditService.logChange(getClientIp(), LogLevel.INFO, "READ_USER", null, "user_details", user.getId(), null, null, null);
        return mapToUserInfoResponse(user);
    }

    public UserInfoResponse getUserInfoByUsername(String username) {
        User user = findByUsername(username);
        auditService.logChange(getClientIp(), LogLevel.INFO, "READ_USER", null, "user_details", user.getId(), null, null, null);
        return mapToUserInfoResponse(user);
    }

    public void changeMyPassword(String username, ChangePasswordRequest request) {
        User user = findByUsername(username);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new CustomException(400, "Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditService.logChange(getClientIp(), LogLevel.INFO, "CHANGE_PASSWORD", null, "password", user.getId(), null, null, null);
    }

    public void changeMyEmail(String username, ChangeEmailRequest request) {
        User user = findByUsername(username);
        String oldEmail = user.getEmail();
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
        auditService.logChange(getClientIp(), LogLevel.INFO, "CHANGE_EMAIL", null, "email", user.getId(), null, oldEmail, request.getNewEmail());
    }

    public void adminChangePassword(String username, AdminChangePasswordRequest request) {
        User user = findByUsername(username);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditService.logChange(getClientIp(), LogLevel.INFO, "CHANGE_PASSWORD", null, "password", user.getId(), null, null, null);
    }

    public void adminChangeEmail(String username, ChangeEmailRequest request) {
        User user = findByUsername(username);
        String oldEmail = user.getEmail();
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
        auditService.logChange(getClientIp(), LogLevel.INFO, "CHANGE_EMAIL", null, "email", user.getId(), null, oldEmail, request.getNewEmail());
    }

    public void updateUserRoles(String username, Set<String> roles) {
        User user = findByUsername(username);
        Set<Role> newRoles = new HashSet<>();
        for (String roleName : roles) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new CustomException(404, "Role not found: " + roleName));
            newRoles.add(role);
        }
        String oldRoles = user.getRoles().toString();
        user.setRoles(newRoles);
        userRepository.save(user);
        auditService.logChange(getClientIp(), LogLevel.INFO, "UPDATE_ROLES", null, "roles", user.getId(), null, oldRoles, newRoles.toString());
    }

    public void deleteUser(String username) {
        User user = findByUsername(username);
        userRepository.delete(user);
        auditService.logChange(getClientIp(), LogLevel.INFO, "DELETE_USER", null, "account", user.getId(), null, null, null);
    }

    // --- Pomocné metody ---

    private UserInfoResponse mapToUserInfoResponse(User user) {
        return new UserInfoResponse(
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }

    private String getClientIp() {
        return httpRequest.getRemoteAddr();
    }
}
