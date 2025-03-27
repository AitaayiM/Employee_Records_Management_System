package com.employee.records.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.employee.records.constant.UserRole;
import com.employee.records.entity.Employee;
import com.employee.records.entity.User;
import com.employee.records.exception.ResourceNotFoundException;
import com.employee.records.exception.UnauthorizedAccessException;
import com.employee.records.repository.EmployeeRepository;
import com.employee.records.repository.UserRepository;
import com.employee.records.service.UserDetailsImpl;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }

        String permissionString = permission.toString();

        // Example: Check if the user has permission to update the employee
        if (permissionString.equals("UPDATE_EMPLOYEE")) {
            Long employeeId = (Long) targetDomainObject;
            return hasUpdateEmployeePermission(authentication, employeeId);
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        // Implement if permissions are checked using a target type (e.g., "EMPLOYEE").
        return false;
    }

    private boolean hasUpdateEmployeePermission(Authentication authentication, Long employeeId) {
        // Logic to check if the user has permission to update the employee
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String email = userDetails.getUsername();

        // Example: Fetch the employee and check if the user is authorized
        // (e.g., ROLE_MANAGER or department match)
        // Use your EmployeeService or repository to fetch employee data.
        // For example:
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("User not found"));

        // Check if user is HR or a manager of the employee's department
        return user.getRole() == UserRole.ROLE_HR || 
        (user.getRole() == UserRole.ROLE_MANAGER && 
        employeeRepository.findByDepartment(employee.getDepartment())
                          .stream()
                          .anyMatch(e -> e.getId().equals(employee.getId())));
    }
}

