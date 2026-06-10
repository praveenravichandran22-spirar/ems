package com.minifullstack.ems.repository;

import com.minifullstack.ems.entity.WorkflowAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowActionRepository extends JpaRepository<WorkflowAction, Long> {

    List<WorkflowAction> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
}