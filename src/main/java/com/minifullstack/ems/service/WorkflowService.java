package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.request.AssignWorkflowRequestDto;
import com.minifullstack.ems.dto.request.WorkflowDecisionRequestDto;
import com.minifullstack.ems.dto.response.EmployeeResponseDto;
import com.minifullstack.ems.dto.response.WorkflowActionDto;

import java.util.List;

public interface WorkflowService {

    EmployeeResponseDto assignWorkflow(Long employeeId, AssignWorkflowRequestDto dto);

    EmployeeResponseDto submit(Long employeeId, String performerEmail);

    EmployeeResponseDto review(Long employeeId, WorkflowDecisionRequestDto dto, String reviewerEmail);

    EmployeeResponseDto approve(Long employeeId, WorkflowDecisionRequestDto dto, String approverEmail);

    List<EmployeeResponseDto> getPendingReview(String reviewerEmail);

    List<EmployeeResponseDto> getPendingApproval(String approverEmail);

    List<WorkflowActionDto> getWorkflowHistory(Long employeeId);
}