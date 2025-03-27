package com.employee.records.service;

import com.employee.records.constant.EmploymentStatus;
import com.employee.records.constant.UserRole;
import com.employee.records.entity.AuditLog;
import com.employee.records.entity.Employee;
import com.employee.records.entity.User;
import com.employee.records.exception.DuplicateEmployeeException;
import com.employee.records.exception.ResourceNotFoundException;
import com.employee.records.repository.AuditLogRepository;
import com.employee.records.repository.EmployeeRepository;
import com.employee.records.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void createEmployee_Success() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmail("test@example.com");
        employee.setFullName("Test Employee");
        employee.setJobTitle("Software Engineer");
        employee.setDepartment("Engineering");
        employee.setHireDate(LocalDate.now());
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setPhone("123-456-7890");
        employee.setAddress("123 Main St");

        User user = new User("testuser@example.com", "password", UserRole.ROLE_HR);

        when(employeeRepository.existsByEmail(employee.getEmail())).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser@example.com");
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(user));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee createdEmployee = employeeService.createEmployee(employee, authentication);

        assertNotNull(createdEmployee);
        assertEquals(employee.getFullName(), createdEmployee.getFullName());
        verify(employeeRepository).save(employee);
        verify(userRepository).save(any(User.class));
        verify(auditLogRepository).save(any());
    }

    @Test
    void createEmployee_DuplicateEmail() {
        Employee employee = new Employee();
        employee.setEmail("test@example.com");

        when(employeeRepository.existsByEmail(employee.getEmail())).thenReturn(true);

        assertThrows(DuplicateEmployeeException.class, () -> employeeService.createEmployee(employee, authentication));

        verify(employeeRepository, never()).save(any(Employee.class));
        verify(userRepository, never()).save(any(User.class));
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void getEmployeeById_Found() {
        Long id = 1L;
        Employee employee = new Employee();
        employee.setId(id);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));

        Employee foundEmployee = employeeService.getEmployeeById(id);

        assertNotNull(foundEmployee);
        assertEquals(id, foundEmployee.getId());
    }

    @Test
    void getEmployeeById_NotFound() {
        Long id = 1L;

        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(id));
    }


    @Test
    void getAllEmployees_NoPagination() {
        List<Employee> employees = List.of(new Employee(), new Employee());
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> allEmployees = employeeService.getAllEmployees();

        assertEquals(2, allEmployees.size());
    }

    @Test
    void getAllEmployees_WithPagination() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        List<Employee> employees = List.of(new Employee(), new Employee());
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());

        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        Page<Employee> allEmployees = employeeService.getAllEmployees(page, size);

        assertEquals(2, allEmployees.getContent().size());
        assertEquals(2, allEmployees.getTotalElements());
    }

    @Test
    void updateEmployee_Success() {
        Long id = 1L;
        Employee existingEmployee = new Employee();
        existingEmployee.setId(id);
        existingEmployee.setEmail("old@example.com");
        existingEmployee.setFullName("Old Name");
        existingEmployee.setJobTitle("Old Title");
        existingEmployee.setDepartment("Old Department");
        existingEmployee.setHireDate(LocalDate.now());
        existingEmployee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        existingEmployee.setPhone("123-456-7890");
        existingEmployee.setAddress("123 Old St");

        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(id);
        updatedEmployee.setEmail("new@example.com");
        updatedEmployee.setFullName("New Name");
        updatedEmployee.setJobTitle("New Title");
        updatedEmployee.setDepartment("New Department");
        updatedEmployee.setHireDate(LocalDate.now());
        updatedEmployee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        updatedEmployee.setPhone("987-654-3210");
        updatedEmployee.setAddress("321 New St");

        User user = new User("testuser@example.com", "password", UserRole.ROLE_HR);
        User associatedUser = new User("old@example.com", "password", UserRole.ROLE_EMPLOYEE);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existingEmployee));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser@example.com");
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("old@example.com")).thenReturn(Optional.of(associatedUser));
        when(employeeRepository.existsByEmail(updatedEmployee.getEmail())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);
        Employee result = employeeService.updateEmployee(id, updatedEmployee, authentication);

        assertNotNull(result);
        assertEquals(updatedEmployee.getFullName(), result.getFullName());
        assertEquals(updatedEmployee.getEmail(), result.getEmail());
        verify(employeeRepository).save(existingEmployee);
        verify(userRepository).save(associatedUser);
        verify(auditLogRepository).save(any());
    }

    @Test
    void updateEmployee_DuplicateEmail() {
        Long id = 1L;
        Employee employee = new Employee();
        employee.setEmail("test@example.com");

        when(employeeRepository.existsByEmail(employee.getEmail())).thenReturn(true);

        assertThrows(DuplicateEmployeeException.class, () -> employeeService.updateEmployee(id, employee, authentication));

        verify(employeeRepository, never()).save(any(Employee.class));
        verify(userRepository, never()).save(any(User.class));
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void deleteEmployee_Success() {
        Long id = 1L;
        User user = new User("testuser@example.com", "password", UserRole.ROLE_HR);
        Employee employee = new Employee();
        employee.setId(id);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser@example.com");
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(user));

        employeeService.deleteEmployee(id, authentication);

        verify(employeeRepository).deleteById(id); // Correct Verification
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void searchEmployees_FullName() {
        String fullName = "Test";
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        List<Employee> employees = List.of(new Employee(), new Employee());
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());

        when(employeeRepository.findByFullNameContainingIgnoreCase(fullName, pageable)).thenReturn(employeePage);

        Page<Employee> result = employeeService.searchEmployees(fullName, null, null, page, size);

        assertEquals(2, result.getContent().size());
    }

    @Test
    void searchEmployees_Department() {
        String department = "Engineering";
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        List<Employee> employees = List.of(new Employee(), new Employee());
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());

        when(employeeRepository.findByDepartmentContainingIgnoreCase(department, pageable)).thenReturn(employeePage);

        Page<Employee> result = employeeService.searchEmployees(null, department, null, page, size);

        assertEquals(2, result.getContent().size());
    }

    @Test
    void searchEmployees_JobTitle() {
        String jobTitle = "Software Engineer";
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        List<Employee> employees = List.of(new Employee(), new Employee());
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());

        when(employeeRepository.findByJobTitleContainingIgnoreCase(jobTitle, pageable)).thenReturn(employeePage);

        Page<Employee> result = employeeService.searchEmployees(null, null, jobTitle, page, size);

        assertEquals(2, result.getContent().size());
    }

    // Add tests for combined search criteria (fullName + department, etc.)
    // ...

    @Test
    void searchEmployees_AllCriteria() {
        String fullName = "Test";
        String department = "Engineering";
        String jobTitle = "Software Engineer";
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        List<Employee> employees = List.of(new Employee(), new Employee());
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());

        when(employeeRepository.findByFullNameContainingIgnoreCaseAndDepartmentContainingIgnoreCaseAndJobTitleContainingIgnoreCase(fullName, department, jobTitle, pageable)).thenReturn(employeePage);

        Page<Employee> result = employeeService.searchEmployees(fullName, department, jobTitle, page, size);

        assertEquals(2, result.getContent().size());
    }

    @Test
    void searchEmployees_NoCriteria() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        List<Employee> employees = List.of(new Employee(), new Employee());
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());

        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        Page<Employee> result = employeeService.searchEmployees(null, null, null, page, size);

        assertEquals(2, result.getContent().size());
    }
}