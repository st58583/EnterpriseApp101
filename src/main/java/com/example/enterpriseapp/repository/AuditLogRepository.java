package com.example.enterpriseapp.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.enterpriseapp.entity.AuditLog;


@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}