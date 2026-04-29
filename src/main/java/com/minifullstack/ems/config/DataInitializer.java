package com.minifullstack.ems.config;

import com.minifullstack.ems.entity.Department;
import com.minifullstack.ems.entity.EmploymentStatus;
import com.minifullstack.ems.repository.DepartmentRepository;
import com.minifullstack.ems.repository.EmploymentStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final EmploymentStatusRepository statusRepository;

    @Override
    public void run(String... args) {
        seedDepartments();
        seedStatuses();
    }

    private void seedDepartments() {
        if (departmentRepository.count() > 0) return;

        List<Department> departments = List.of(
                Department.builder().name("Engineering").description("Software and hardware engineering").build(),
                Department.builder().name("Human Resources").description("People operations and talent management").build(),
                Department.builder().name("Finance").description("Financial planning and accounting").build(),
                Department.builder().name("Marketing").description("Brand, growth and communications").build(),
                Department.builder().name("Sales").description("Revenue generation and client relations").build(),
                Department.builder().name("Operations").description("Business operations and process management").build(),
                Department.builder().name("Legal").description("Legal affairs and compliance").build(),
                Department.builder().name("Design").description("UX, UI and product design").build()
        );

        departmentRepository.saveAll(departments);
        System.out.println("✔ Seeded " + departments.size() + " departments");
    }

    private void seedStatuses() {
        if (statusRepository.count() > 0) return;

        List<EmploymentStatus> statuses = List.of(
                EmploymentStatus.builder().name("Active").description("Currently employed and working").build(),
                EmploymentStatus.builder().name("Inactive").description("Employment paused or not yet started").build(),
                EmploymentStatus.builder().name("On Leave").description("Temporarily away from work").build(),
                EmploymentStatus.builder().name("Terminated").description("Employment ended").build(),
                EmploymentStatus.builder().name("Probation").description("Under probationary period").build()
        );

        statusRepository.saveAll(statuses);
        System.out.println("✔ Seeded " + statuses.size() + " employment statuses");
    }
}
