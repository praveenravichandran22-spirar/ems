package com.minifullstack.ems.service.impl;

import com.minifullstack.ems.dto.request.AssignWorkflowRequestDto;
import com.minifullstack.ems.dto.request.WorkflowDecisionRequestDto;
import com.minifullstack.ems.dto.response.EmployeeResponseDto;
import com.minifullstack.ems.dto.response.WorkflowActionDto;
import com.minifullstack.ems.entity.Employee;
import com.minifullstack.ems.entity.User;
import com.minifullstack.ems.entity.WorkflowAction;
import com.minifullstack.ems.enums.Role;
import com.minifullstack.ems.enums.WorkflowActionType;
import com.minifullstack.ems.enums.WorkflowStatus;
import com.minifullstack.ems.exception.ResourceNotFoundException;
import com.minifullstack.ems.repository.EmployeeRepository;
import com.minifullstack.ems.repository.UserRepository;
import com.minifullstack.ems.repository.WorkflowActionRepository;
import com.minifullstack.ems.service.EmployeeService;
import com.minifullstack.ems.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowServiceImpl implements WorkflowService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final WorkflowActionRepository workflowActionRepository;
    private final EmployeeService employeeService;

    @Override
    public EmployeeResponseDto assignWorkflow(Long employeeId, AssignWorkflowRequestDto dto) {
        Employee employee = findEmployee(employeeId);

        List<User> reviewers = dto.getReviewerIds() != null
                ? userRepository.findAllById(dto.getReviewerIds())
                : List.of();
        List<User> approvers = dto.getApproverIds() != null
                ? userRepository.findAllById(dto.getApproverIds())
                : List.of();

        reviewers.forEach(u -> {
            if (u.getRole() != Role.ROLE_REVIEWER)
                throw new IllegalArgumentException(u.getEmail() + " does not have the Reviewer role");
        });
        approvers.forEach(u -> {
            if (u.getRole() != Role.ROLE_APPROVER)
                throw new IllegalArgumentException(u.getEmail() + " does not have the Approver role");
        });

        employee.setAssignedReviewers(reviewers);
        employee.setAssignedApprovers(approvers);
        employeeRepository.save(employee);
        return employeeService.getById(employeeId);
    }

    @Override
    public EmployeeResponseDto submit(Long employeeId, String performerEmail) {
        Employee employee = findEmployee(employeeId);

        if (employee.getWorkflowStatus() != WorkflowStatus.DRAFT
                && employee.getWorkflowStatus() != WorkflowStatus.REJECTED) {
            throw new IllegalStateException(
                    "Only DRAFT or REJECTED employees can be submitted. Current status: " + employee.getWorkflowStatus());
        }
        if (employee.getAssignedReviewers().isEmpty()) {
            throw new IllegalStateException("Assign at least one Reviewer before submitting");
        }
        if (employee.getAssignedApprovers().isEmpty()) {
            throw new IllegalStateException("Assign at least one Approver before submitting");
        }

        employee.setWorkflowStatus(WorkflowStatus.IN_REVIEW);
        employeeRepository.save(employee);

        recordAction(employee, performerEmail, WorkflowActionType.SUBMITTED, "Submitted for review");
        return employeeService.getById(employeeId);
    }

    @Override
    public EmployeeResponseDto review(Long employeeId, WorkflowDecisionRequestDto dto, String reviewerEmail) {
        Employee employee = findEmployee(employeeId);

        if (employee.getWorkflowStatus() != WorkflowStatus.IN_REVIEW) {
            throw new IllegalStateException(
                    "Employee is not in IN_REVIEW status. Current status: " + employee.getWorkflowStatus());
        }

        boolean isAssigned = employee.getAssignedReviewers().stream()
                .anyMatch(u -> u.getEmail().equals(reviewerEmail));
        if (!isAssigned) {
            throw new IllegalStateException("You are not an assigned Reviewer for this employee");
        }
        if (dto.getNote() == null || dto.getNote().isBlank()) {
            throw new IllegalArgumentException("A note is required when submitting a review decision");
        }

        if ("APPROVE".equalsIgnoreCase(dto.getDecision())) {
            employee.setWorkflowStatus(WorkflowStatus.IN_APPROVAL);
            recordAction(employee, reviewerEmail, WorkflowActionType.REVIEW_APPROVED, dto.getNote());
        } else if ("REJECT".equalsIgnoreCase(dto.getDecision())) {
            employee.setWorkflowStatus(WorkflowStatus.REJECTED);
            recordAction(employee, reviewerEmail, WorkflowActionType.REVIEW_REJECTED, dto.getNote());
        } else {
            throw new IllegalArgumentException("Decision must be APPROVE or REJECT");
        }

        employeeRepository.save(employee);
        return employeeService.getById(employeeId);
    }

    @Override
    public EmployeeResponseDto approve(Long employeeId, WorkflowDecisionRequestDto dto, String approverEmail) {
        Employee employee = findEmployee(employeeId);

        if (employee.getWorkflowStatus() != WorkflowStatus.IN_APPROVAL) {
            throw new IllegalStateException(
                    "Employee is not in IN_APPROVAL status. Current status: " + employee.getWorkflowStatus());
        }

        boolean isAssigned = employee.getAssignedApprovers().stream()
                .anyMatch(u -> u.getEmail().equals(approverEmail));
        if (!isAssigned) {
            throw new IllegalStateException("You are not an assigned Approver for this employee");
        }
        if (dto.getNote() == null || dto.getNote().isBlank()) {
            throw new IllegalArgumentException("A note is required when submitting an approval decision");
        }

        if ("APPROVE".equalsIgnoreCase(dto.getDecision())) {
            employee.setWorkflowStatus(WorkflowStatus.APPROVED);
            recordAction(employee, approverEmail, WorkflowActionType.FINAL_APPROVED, dto.getNote());
        } else if ("REJECT".equalsIgnoreCase(dto.getDecision())) {
            employee.setWorkflowStatus(WorkflowStatus.REJECTED);
            recordAction(employee, approverEmail, WorkflowActionType.FINAL_REJECTED, dto.getNote());
        } else {
            throw new IllegalArgumentException("Decision must be APPROVE or REJECT");
        }

        employeeRepository.save(employee);
        return employeeService.getById(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> getPendingReview(String reviewerEmail) {
        return employeeRepository
                .findByReviewerEmailAndStatus(reviewerEmail, WorkflowStatus.IN_REVIEW)
                .stream()
                .map(e -> employeeService.getById(e.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> getPendingApproval(String approverEmail) {
        return employeeRepository
                .findByApproverEmailAndStatus(approverEmail, WorkflowStatus.IN_APPROVAL)
                .stream()
                .map(e -> employeeService.getById(e.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowActionDto> getWorkflowHistory(Long employeeId) {
        findEmployee(employeeId); // ensure exists
        return workflowActionRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream()
                .map(this::toActionDto)
                .toList();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    private void recordAction(Employee employee, String performerEmail,
                               WorkflowActionType actionType, String note) {
        User performer = userRepository.findByEmail(performerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + performerEmail));
        workflowActionRepository.save(WorkflowAction.builder()
                .employee(employee)
                .performedBy(performer)
                .actionType(actionType)
                .note(note)
                .build());
    }

    private WorkflowActionDto toActionDto(WorkflowAction a) {
        String name = a.getPerformedBy() != null
                ? a.getPerformedBy().getFirstName() + " " + a.getPerformedBy().getLastName()
                : "System";
        String email = a.getPerformedBy() != null ? a.getPerformedBy().getEmail() : "";
        return WorkflowActionDto.builder()
                .id(a.getId())
                .performedByName(name)
                .performedByEmail(email)
                .actionType(a.getActionType())
                .note(a.getNote())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
