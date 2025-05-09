package com.example.enterpriseapp.service;

import org.springframework.stereotype.Service;
import com.example.enterpriseapp.entity.AuditLog;
import com.example.enterpriseapp.entity.LogLevel;
import com.example.enterpriseapp.repository.AuditLogRepository;

import java.math.BigInteger;
import java.time.LocalDateTime;


@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logChange(String ipAddress, LogLevel logLevel, String actionType, Long actorUserId, String entityName, Long entityId,
                          String fieldName, String oldValue, String newValue) {

        AuditLog auditLog = new AuditLog();
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setActorUserId(actorUserId);
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setIpAddress(ipAddress);
        auditLog.setLogLevel(logLevel);
        auditLog.setFieldName(fieldName);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);
        auditLog.setActionType(actionType);

        auditLogRepository.save(auditLog);
    }
}

