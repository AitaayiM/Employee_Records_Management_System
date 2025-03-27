package com.employee.records.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employee.records.entity.Employee;

import java.util.List;

//Employee Repository
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Derived Queries (for searching/filtering)

    List<Employee> findByDepartment(String department);
    
    Page<Employee> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

    Page<Employee> findByDepartmentContainingIgnoreCase(String department, Pageable pageable);

    Page<Employee> findByJobTitleContainingIgnoreCase(String jobTitle, Pageable pageable);

    Page<Employee> findByFullNameContainingIgnoreCaseAndDepartmentContainingIgnoreCase(String fullName, String department, Pageable pageable);

    Page<Employee> findByFullNameContainingIgnoreCaseAndJobTitleContainingIgnoreCase(String fullName, String jobTitle, Pageable pageable);

    Page<Employee> findByDepartmentContainingIgnoreCaseAndJobTitleContainingIgnoreCase(String department, String jobTitle, Pageable pageable);

    Page<Employee> findByFullNameContainingIgnoreCaseAndDepartmentContainingIgnoreCaseAndJobTitleContainingIgnoreCase(String fullName, String department, String jobTitle, Pageable pageable);

    boolean existsByEmail(String email);

}
