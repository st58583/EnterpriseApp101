package com.example.enterpriseapp.controller;

import com.example.enterpriseapp.common.ApiResponse;
import com.example.enterpriseapp.dto.AdminChangePasswordRequest;
import com.example.enterpriseapp.dto.ChangeEmailRequest;
import com.example.enterpriseapp.dto.ChangePasswordRequest;
import com.example.enterpriseapp.dto.UserInfoResponse;
import com.example.enterpriseapp.entity.LogLevel;
import com.example.enterpriseapp.entity.Role;
import com.example.enterpriseapp.entity.User;
import com.example.enterpriseapp.exception.CustomException;
import com.example.enterpriseapp.repository.RoleRepository;
import com.example.enterpriseapp.repository.UserRepository;
import com.example.enterpriseapp.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuditService auditService;
    private final HttpServletRequest httpRequest;

    public UserController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AuditService auditService, HttpServletRequest httpRequest) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.httpRequest = httpRequest;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User actorUser = findUserOrThrow(userDetails.getUsername());
        UserInfoResponse response = mapToUserInfoResponse(actorUser);
        auditService.logChange(httpRequest.getRemoteAddr(), LogLevel.INFO, "READ_USER", actorUser.getId(), "user_details", actorUser.getId(), null, null, null);
        return ResponseEntity.ok(ApiResponse.ok("User details retrieved successfully", response));
    }

    @PatchMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        User actorUser = findUserOrThrow(userDetails.getUsername());
        auditService.logChange(httpRequest.getRemoteAddr(), LogLevel.INFO, "CHANGE_PASSWORD", actorUser.getId(), "password", actorUser.getId(), null, null, null);
        return changePassword(actorUser.getUsername(), request, true);
    }

    @PatchMapping("/me/change-email")
    public ResponseEntity<ApiResponse<Void>> changeMyEmail(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChangeEmailRequest request
    ) {
        User actorUser = findUserOrThrow(userDetails.getUsername());
        auditService.logChange(httpRequest.getRemoteAddr(), LogLevel.INFO, "CHANGE_EMAIL", actorUser.getId(), "email", actorUser.getId(), null, actorUser.getEmail(), request.getNewEmail());
        return changeEmail(actorUser.getUsername(), request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserByUsername(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username
    ) {
        User actorUser = findUserOrThrow(userDetails.getUsername());
        User user = findUserOrThrow(username);
        UserInfoResponse response = mapToUserInfoResponse(user);
        auditService.logChange(httpRequest.getRemoteAddr(), LogLevel.INFO, "READ_USER", actorUser.getId(), "user_details", user.getId(), null, null, null);
        return ResponseEntity.ok(ApiResponse.ok("User details retrieved successfully", response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{username}/change-password")
    public ResponseEntity<ApiResponse<Void>> adminChangePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username,
            @RequestBody @Valid AdminChangePasswordRequest request
    ) {
        User actorUser = findUserOrThrow(userDetails.getUsername());
        User user = findUserOrThrow(username);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditService.logChange(httpRequest.getRemoteAddr(), LogLevel.INFO, "CHANGE_PASSWORD", actorUser.getId(), "password", user.getId(), null, null, null);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{username}/change-email")
    public ResponseEntity<ApiResponse<Void>> adminChangeEmail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username,
            @RequestBody ChangeEmailRequest request
    ) {
        User actorUser = findUserOrThrow(userDetails.getUsername());
        User user = findUserOrThrow(username);
        auditService.logChange(httpRequest.getRemoteAddr(), LogLevel.INFO, "CHANGE_EMAIL", actorUser.getId(), "email", user.getId(), null, user.getEmail(), request.getNewEmail());
        return changeEmail(username, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username
    ) {
        User actorUser = findUserOrThrow(userDetails.getUsername());
        User user = findUserOrThrow(username);
        userRepository.delete(user);
        auditService.logChange(httpRequest.getRemoteAddr(), LogLevel.INFO, "DELETE_USER", actorUser.getId(), "account", user.getId(), null, null, null);
        return ResponseEntity.ok(ApiResponse.ok("User deleted successfully", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{username}/roles")
    public ResponseEntity<ApiResponse<Void>> updateUserRoles(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username,
            @RequestBody Set<String> roles
    ) {
        User actorUser = findUserOrThrow(userDetails.getUsername());
        User user = findUserOrThrow(username);

        Set<Role> newRoles = new HashSet<>();
        for (String roleName : roles) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new CustomException(404, "Role not found: " + roleName));
            newRoles.add(role);
        }

        auditService.logChange(httpRequest.getRemoteAddr(), LogLevel.INFO, "UPDATE_ROLES", actorUser.getId(), "roles", user.getId(), null, user.getRoles().toString(), newRoles.toString());

        user.setRoles(newRoles);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.ok("User roles updated successfully", null));
    }

    // --- Pomocné metody ---

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(404, "User not found"));
    }

    private UserInfoResponse mapToUserInfoResponse(User user) {
        return new UserInfoResponse(
                user.getUsername(),
                user.getEmail(),
                user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }

    private ResponseEntity<ApiResponse<Void>> changePassword(String username, ChangePasswordRequest request, boolean validateOldPassword) {
        User user = findUserOrThrow(username);

        if (validateOldPassword) {
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new CustomException(400, "Old password is incorrect");
            }
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    private ResponseEntity<ApiResponse<Void>> changeEmail(String username, ChangeEmailRequest request) {
        User user = findUserOrThrow(username);

        user.setEmail(request.getNewEmail());
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.ok("Email changed successfully", null));
    }
}
