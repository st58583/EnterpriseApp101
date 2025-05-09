package com.example.enterpriseapp.security;

import com.example.enterpriseapp.entity.AuditLog;
import com.example.enterpriseapp.entity.LogLevel;
import com.example.enterpriseapp.repository.AuditLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private final AuditLogRepository auditLogRepository;

    public RequestLoggingFilter(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String ipAddress = request.getRemoteAddr();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails customUser) {
            userId = customUser.getUser().getId();
        }

        filterChain.doFilter(request, response);

        int status = response.getStatus();
        boolean isError = status >= 400;

        AuditLog log = new AuditLog();
        log.setTimestamp(LocalDateTime.now());
        log.setActorUserId(userId);
        log.setEntityName("HttpRequest");
        log.setEntityId(null);
        log.setFieldName("endpoint");
        log.setOldValue(null);
        log.setNewValue(method + " " + path);
        log.setActionType("ACCESS");
        log.setIpAddress(ipAddress);
        log.setLogLevel(isError ? LogLevel.WARN : LogLevel.INFO);

        auditLogRepository.save(log);

        if (isError) {
            logger.warn("Request to {} {} resulted in status {}", method, path, status);
        } else {
            logger.info("Request to {} {} processed with status {}", method, path, status);
        }
    }
}
