package com.employee.records.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_log_employee_id", columnList = "employee_id")
})
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    @NotNull
    private String employeeId;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @NotNull
    private User changedBy;

    @NotNull
    private LocalDateTime changeTime;

    @NotBlank
    @Size(max = 500)
    private String changeDescription;
    
}
