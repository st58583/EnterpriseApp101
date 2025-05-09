package com.example.enterpriseapp.controller;

import com.example.enterpriseapp.common.ApiResponse;
import com.example.enterpriseapp.dto.LoginRequest;
import com.example.enterpriseapp.dto.RegisterRequest;
import com.example.enterpriseapp.dto.RefreshTokenRequest;
import com.example.enterpriseapp.entity.User;
import com.example.enterpriseapp.exception.CustomException;
import com.example.enterpriseapp.security.JwtUtil;
import com.example.enterpriseapp.service.UserService;
import com.example.enterpriseapp.entity.LogLevel;
import com.example.enterpriseapp.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;
    private final HttpServletRequest httpRequest;

    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil, AuditService auditService, HttpServletRequest httpRequest) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.auditService = auditService;
        this.httpRequest = httpRequest;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest request) {
        userService.register(request);

        auditService.logChange(httpRequest.getRemoteAddr(), LogLevel.INFO, "CREATE_USER", null, "User", null, null, null, null);

        return ResponseEntity.ok(ApiResponse.ok("User registered successfully", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            auditService.logChange(httpRequest.getRemoteAddr(), LogLevel.WARN, "LOGIN_FAILED", null, "User", null, null, null, request.getUsername());
            throw new CustomException(401, "Invalid username or password");
        }

        // Najdeme uživatele podle username (login se přihlašuje přes username)
        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            throw new CustomException(404, "User not found after authentication");
        }

        auditService.logChange(httpRequest.getRemoteAddr(), LogLevel.INFO, "LOGIN_SUCCESS", null, "User", null, null, null, null);

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return ResponseEntity.ok(ApiResponse.ok("Login successful", tokens));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        try {
            // Nově získáme ID uživatele z tokenu
            Long userId = jwtUtil.extractUserId(refreshToken);

            User user = userService.findById(userId);
            if (user == null) {
                throw new CustomException(404, "User not found");
            }

            String newAccessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId());

            auditService.logChange(httpRequest.getRemoteAddr(), LogLevel.INFO, "REFRESH_TOKEN", null, "User", null, null, null, null);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);

            return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", tokens));
        } catch (Exception e) {
            auditService.logChange(httpRequest.getRemoteAddr(), LogLevel.WARN, "INVALID_REFRESH_TOKEN", null, "User", null, null, null, null);

            throw new CustomException(401, "Invalid refresh token");
        }
    }
}
