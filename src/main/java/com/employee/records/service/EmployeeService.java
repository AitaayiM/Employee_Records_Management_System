package com.employee.records.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.employee.records.constant.UserRole;
import com.employee.records.entity.AuditLog;
import com.employee.records.entity.Employee;
import com.employee.records.entity.User;
import com.employee.records.exception.DuplicateEmployeeException;
import com.employee.records.exception.ResourceNotFoundException;
import com.employee.records.exception.UnauthorizedAccessException;
import com.employee.records.repository.AuditLogRepository;
import com.employee.records.repository.EmployeeRepository;
import com.employee.records.repository.UserRepository;

import jakarta.validation.Valid;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;  // Assuming a UserRepository exists

    @Autowired
    private AuditLogRepository auditLogRepository;

    public Employee createEmployee(@Valid Employee employee, Authentication authentication) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new DuplicateEmployeeException("Employee with email " + employee.getEmail() + " already exists");
        }
        if (userRepository.existsByEmail(employee.getEmail())) {
            throw new DuplicateEmployeeException("User with email " + employee.getEmail() + " already exists");
        }        
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUsername = userDetails.getUsername();
        User userPerformingAction = findUserByUsername(currentUsername);
        // We can use authorize() method instead of @PreAuthorize
        // authorize(userPerformingAction, UserRole.ROLE_HR);
        
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // By default the password is the email itself
        String hashedPassword = passwordEncoder.encode(employee.getEmail());
        User newUser = new User(employee.getEmail(), hashedPassword, UserRole.ROLE_EMPLOYEE);
        newUser.setActive(true);
        userRepository.save(newUser);

        Employee savedEmployee = employeeRepository.save(employee);
        logAudit(userPerformingAction, savedEmployee.getId(), "Created employee: " + savedEmployee.getFullName());
        return savedEmployee;
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Page<Employee> getAllEmployees(int page, int size) {
        Pageable pageable = PageRequest.of(page, size); // Page index starts from 0
        return employeeRepository.findAll(pageable);
    }

    public Employee updateEmployee(Long id, @Valid Employee employee, Authentication authentication) {
        Employee existingEmployee = getEmployeeById(id);
        
        if (!existingEmployee.getEmail().equals(employee.getEmail())) {
            if (userRepository.existsByEmail(employee.getEmail())) {
                throw new DuplicateEmployeeException("User with email " + employee.getEmail() + " already exists");
            }
            
            // Update User email if Employee email has changed
            User associatedUser = userRepository.findByEmail(existingEmployee.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User with email " + existingEmployee.getEmail() + " not found"));
            associatedUser.setEmail(employee.getEmail());
            userRepository.save(associatedUser);
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUsername = userDetails.getUsername();
        User userPerformingAction = findUserByUsername(currentUsername);

        // We can use authorizeUpdate() method instead of @PreAuthorize
        // authorizeUpdate(userPerformingAction, existingEmployee);

        String changes = getChanges(existingEmployee, employee);

        existingEmployee.setFullName(employee.getFullName());
        existingEmployee.setJobTitle(employee.getJobTitle());
        existingEmployee.setDepartment(employee.getDepartment());
        existingEmployee.setHireDate(employee.getHireDate());
        existingEmployee.setEmploymentStatus(employee.getEmploymentStatus());
        existingEmployee.setEmail(employee.getEmail());
        existingEmployee.setPhone(employee.getPhone());
        existingEmployee.setAddress(employee.getAddress());

        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        logAudit(userPerformingAction, updatedEmployee.getId(), "Updated employee: " + updatedEmployee.getFullName() + ". Changes: " + changes);
        return updatedEmployee;
    }

    public void deleteEmployee(Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUsername = userDetails.getUsername();
        User userPerformingAction = findUserByUsername(currentUsername);
        // We can use authorize() method instead of @PreAuthorize
        // authorize(userPerformingAction, UserRole.ROLE_HR);

        employeeRepository.deleteById(id);
        logAudit(userPerformingAction, id, "Deleted employee with ID: " + id);
    }

    public Page<Employee> searchEmployees(String fullName, String department, String jobTitle, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (fullName != null && !fullName.isEmpty() && department != null && !department.isEmpty() && jobTitle != null && !jobTitle.isEmpty()) {
            return employeeRepository.findByFullNameContainingIgnoreCaseAndDepartmentContainingIgnoreCaseAndJobTitleContainingIgnoreCase(fullName, department, jobTitle, pageable);
        } else if (fullName != null && !fullName.isEmpty() && department != null && !department.isEmpty()) {
            return employeeRepository.findByFullNameContainingIgnoreCaseAndDepartmentContainingIgnoreCase(fullName, department, pageable);
        } else if (fullName != null && !fullName.isEmpty() && jobTitle != null && !jobTitle.isEmpty()) {
            return employeeRepository.findByFullNameContainingIgnoreCaseAndJobTitleContainingIgnoreCase(fullName, jobTitle, pageable);
        } else if (department != null && !department.isEmpty() && jobTitle != null && !jobTitle.isEmpty()) {
            return employeeRepository.findByDepartmentContainingIgnoreCaseAndJobTitleContainingIgnoreCase(department, jobTitle, pageable);
        } else if (fullName != null && !fullName.isEmpty()) {
            return employeeRepository.findByFullNameContainingIgnoreCase(fullName, pageable);
        } else if (department != null && !department.isEmpty()) {
            return employeeRepository.findByDepartmentContainingIgnoreCase(department, pageable);
        } else if (jobTitle != null && !jobTitle.isEmpty()) {
            return employeeRepository.findByJobTitleContainingIgnoreCase(jobTitle, pageable);
        } else {
            return employeeRepository.findAll(pageable);
        }
    }

    private User findUserByUsername(String username) {
        return userRepository.findByEmail(username).orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    // We can use authorize() method instead of @PreAuthorize
    private void authorize(User user, UserRole requiredRole) {
        if (!user.getRole().equals(requiredRole)) {
            throw new UnauthorizedAccessException("Unauthorized access. Required role: " + requiredRole);
        }
    }

    // We can use authorizeUpdate() method instead of @PreAuthorize
    private void authorizeUpdate(User user, Employee employee) {
        if (user.getRole() == UserRole.ROLE_HR) {
            return; // HR can update any employee
        }
    
        if (user.getRole() == UserRole.ROLE_MANAGER) {
            List<Employee> managedEmployees = employeeRepository.findByDepartment(employee.getDepartment());
            boolean isAuthorized = managedEmployees.stream()
                    .anyMatch(e -> e.getId().equals(employee.getId()));
            if (isAuthorized) {
                return;
            }
        }
    
        throw new UnauthorizedAccessException("You are not authorized to update this employee.");
    }
    

    private void logAudit(User user, Long employeeId, String description) {
        AuditLog auditLog = new AuditLog();
        auditLog.setEmployeeId(employeeId.toString());
        auditLog.setChangedBy(user);
        auditLog.setChangeTime(LocalDateTime.now());
        auditLog.setChangeDescription(description);
        auditLogRepository.save(auditLog);
    }

    private String getChanges(Employee existingEmployee, Employee updatedEmployee) {
        StringBuilder changes = new StringBuilder();

        if (!existingEmployee.getFullName().equals(updatedEmployee.getFullName())) {
            changes.append("Full Name: ").append(existingEmployee.getFullName()).append(" -> ").append(updatedEmployee.getFullName()).append(", ");
        }
        if (!existingEmployee.getJobTitle().equals(updatedEmployee.getJobTitle())) {
            changes.append("Job Title: ").append(existingEmployee.getJobTitle()).append(" -> ").append(updatedEmployee.getJobTitle()).append(", ");
        }
        if (!existingEmployee.getDepartment().equals(updatedEmployee.getDepartment())) {
            changes.append("Department: ").append(existingEmployee.getDepartment()).append(" -> ").append(updatedEmployee.getDepartment()).append(", ");
        }
        if (!existingEmployee.getHireDate().equals(updatedEmployee.getHireDate())) {
            changes.append("Hire Date: ").append(existingEmployee.getHireDate()).append(" -> ").append(updatedEmployee.getHireDate()).append(", ");
        }
        if (!existingEmployee.getEmploymentStatus().equals(updatedEmployee.getEmploymentStatus())) {
            changes.append("Employment Status: ").append(existingEmployee.getEmploymentStatus()).append(" -> ").append(updatedEmployee.getEmploymentStatus()).append(", ");
        }
        if (!Objects.equals(existingEmployee.getEmail(), updatedEmployee.getEmail())) { // Handle nulls
            changes.append("Email: ").append(existingEmployee.getEmail()).append(" -> ").append(updatedEmployee.getEmail()).append(", ");
        }
        if (!Objects.equals(existingEmployee.getPhone(), updatedEmployee.getPhone())) { // Handle nulls
            changes.append("Phone: ").append(existingEmployee.getPhone()).append(" -> ").append(updatedEmployee.getPhone()).append(", ");
        }
        if (!Objects.equals(existingEmployee.getAddress(), updatedEmployee.getAddress())) { // Handle nulls
            changes.append("Address: ").append(existingEmployee.getAddress()).append(" -> ").append(updatedEmployee.getAddress()).append(", ");
        }

        // Remove trailing comma and space if there are any changes
        if (changes.length() > 0) {
            return changes.substring(0, changes.length() - 2);
        } else {
            return ""; // Return empty string if no changes
        }
    }
}
