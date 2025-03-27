package com.employee.records.entity;

import java.time.LocalDate;

import com.employee.records.constant.EmploymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_full_name", columnList = "fullName"),
        @Index(name = "idx_department", columnList = "department")
})
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must be less than 255 characters")
    @Column(nullable = false)
    private String fullName;

    @NotBlank(message = "Job title is required")
    @Size(max = 255, message = "Job title must be less than 255 characters")
    @Column(nullable = false)
    private String jobTitle;

    @NotBlank(message = "Department is required")
    @Size(max = 255, message = "Department must be less than 255 characters")
    @Column(nullable = false)
    private String department;

    @NotNull(message = "Hire date is required")
    @Column(nullable = false)
    private LocalDate hireDate;

    @NotNull(message = "Employment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmploymentStatus employmentStatus;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be less than 255 characters")
    @Column(unique = true)
    private String email;

    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String phone;

    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;

    
}
