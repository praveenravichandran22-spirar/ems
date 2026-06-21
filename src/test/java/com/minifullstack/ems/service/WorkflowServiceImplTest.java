package com.minifullstack.ems.service;

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
import com.minifullstack.ems.service.impl.WorkflowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceImplTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private UserRepository userRepository;
    @Mock private WorkflowActionRepository workflowActionRepository;
    @Mock private EmployeeService employeeService;

    @InjectMocks private WorkflowServiceImpl workflowService;

    private User reviewer;
    private User approver;
    private User admin;
    private Employee employee;
    private EmployeeResponseDto stubResponse;

    @BeforeEach
    void setUp() {
        reviewer = User.builder().id(10L).email("reviewer@test.com")
                .role(Role.ROLE_REVIEWER).firstName("Rev").lastName("User").build();
        approver = User.builder().id(11L).email("approver@test.com")
                .role(Role.ROLE_APPROVER).firstName("App").lastName("User").build();
        admin    = User.builder().id(1L).email("admin@test.com")
                .role(Role.ROLE_ADMIN).firstName("Ad").lastName("Min").build();

        employee = Employee.builder()
                .id(1L).firstName("John").lastName("Doe").email("john@emp.com")
                .isRemote(false).joiningDate(java.time.LocalDate.now())
                .workflowStatus(WorkflowStatus.DRAFT)
                .assignedReviewers(new ArrayList<>(List.of(reviewer)))
                .assignedApprovers(new ArrayList<>(List.of(approver)))
                .build();

        stubResponse = EmployeeResponseDto.builder().id(1L).build();
    }

    // ── assignWorkflow ────────────────────────────────────────────────────────

    @Test
    void assignWorkflow_assignsReviewersAndApprovers() {
        AssignWorkflowRequestDto dto = new AssignWorkflowRequestDto();
        dto.setReviewerIds(List.of(10L));
        dto.setApproverIds(List.of(11L));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findAllById(List.of(10L))).thenReturn(List.of(reviewer));
        when(userRepository.findAllById(List.of(11L))).thenReturn(List.of(approver));
        when(employeeRepository.save(any())).thenReturn(employee);
        when(employeeService.getById(1L)).thenReturn(stubResponse);

        EmployeeResponseDto result = workflowService.assignWorkflow(1L, dto);

        assertThat(result.getId()).isEqualTo(1L);
        verify(employeeRepository).save(employee);
    }

    @Test
    void assignWorkflow_throwsWhenReviewerHasWrongRole() {
        User wrongRole = User.builder().id(5L).email("bad@test.com").role(Role.ROLE_USER).build();
        AssignWorkflowRequestDto dto = new AssignWorkflowRequestDto();
        dto.setReviewerIds(List.of(5L));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findAllById(List.of(5L))).thenReturn(List.of(wrongRole));

        assertThatThrownBy(() -> workflowService.assignWorkflow(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not have the Reviewer role");
    }

    @Test
    void assignWorkflow_throwsWhenApproverHasWrongRole() {
        User wrongRole = User.builder().id(5L).email("bad@test.com").role(Role.ROLE_USER).build();
        AssignWorkflowRequestDto dto = new AssignWorkflowRequestDto();
        dto.setReviewerIds(List.of(10L));
        dto.setApproverIds(List.of(5L));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findAllById(List.of(10L))).thenReturn(List.of(reviewer));
        when(userRepository.findAllById(List.of(5L))).thenReturn(List.of(wrongRole));

        assertThatThrownBy(() -> workflowService.assignWorkflow(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not have the Approver role");
    }

    @Test
    void assignWorkflow_handlesNullReviewerIds() {
        AssignWorkflowRequestDto dto = new AssignWorkflowRequestDto();
        dto.setReviewerIds(null);
        dto.setApproverIds(null);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);
        when(employeeService.getById(1L)).thenReturn(stubResponse);

        workflowService.assignWorkflow(1L, dto);

        assertThat(employee.getAssignedReviewers()).isEmpty();
        assertThat(employee.getAssignedApprovers()).isEmpty();
    }

    // ── submit ────────────────────────────────────────────────────────────────

    @Test
    void submit_changesStatusToInReview_whenDraft() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(workflowActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(employeeService.getById(1L)).thenReturn(stubResponse);

        workflowService.submit(1L, "admin@test.com");

        assertThat(employee.getWorkflowStatus()).isEqualTo(WorkflowStatus.IN_REVIEW);
        verify(employeeRepository).save(employee);
    }

    @Test
    void submit_changesStatusToInReview_whenRejected() {
        employee.setWorkflowStatus(WorkflowStatus.REJECTED);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(workflowActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(employeeService.getById(1L)).thenReturn(stubResponse);

        workflowService.submit(1L, "admin@test.com");

        assertThat(employee.getWorkflowStatus()).isEqualTo(WorkflowStatus.IN_REVIEW);
    }

    @Test
    void submit_throwsWhenStatusIsNotDraftOrRejected() {
        employee.setWorkflowStatus(WorkflowStatus.APPROVED);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> workflowService.submit(1L, "admin@test.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only DRAFT or REJECTED");
    }

    @Test
    void submit_throwsWhenNoReviewersAssigned() {
        employee.setAssignedReviewers(new ArrayList<>());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> workflowService.submit(1L, "admin@test.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Assign at least one Reviewer");
    }

    @Test
    void submit_throwsWhenNoApproversAssigned() {
        employee.setAssignedApprovers(new ArrayList<>());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> workflowService.submit(1L, "admin@test.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Assign at least one Approver");
    }

    @Test
    void submit_throwsWhenEmployeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workflowService.submit(99L, "admin@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found");
    }

    // ── review ────────────────────────────────────────────────────────────────

    @Test
    void review_approve_changesStatusToInApproval() {
        employee.setWorkflowStatus(WorkflowStatus.IN_REVIEW);
        WorkflowDecisionRequestDto dto = decisionDto("APPROVE", "Looks good");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findByEmail("reviewer@test.com")).thenReturn(Optional.of(reviewer));
        when(workflowActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(employeeRepository.save(any())).thenReturn(employee);
        when(employeeService.getById(1L)).thenReturn(stubResponse);

        workflowService.review(1L, dto, "reviewer@test.com");

        assertThat(employee.getWorkflowStatus()).isEqualTo(WorkflowStatus.IN_APPROVAL);
    }

    @Test
    void review_reject_changesStatusToRejected() {
        employee.setWorkflowStatus(WorkflowStatus.IN_REVIEW);
        WorkflowDecisionRequestDto dto = decisionDto("REJECT", "Needs work");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findByEmail("reviewer@test.com")).thenReturn(Optional.of(reviewer));
        when(workflowActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(employeeRepository.save(any())).thenReturn(employee);
        when(employeeService.getById(1L)).thenReturn(stubResponse);

        workflowService.review(1L, dto, "reviewer@test.com");

        assertThat(employee.getWorkflowStatus()).isEqualTo(WorkflowStatus.REJECTED);
    }

    @Test
    void review_throwsWhenEmployeeNotInReview() {
        employee.setWorkflowStatus(WorkflowStatus.DRAFT);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> workflowService.review(1L, decisionDto("APPROVE", "ok"), "reviewer@test.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not in IN_REVIEW status");
    }

    @Test
    void review_throwsWhenReviewerNotAssigned() {
        employee.setWorkflowStatus(WorkflowStatus.IN_REVIEW);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> workflowService.review(1L, decisionDto("APPROVE", "ok"), "other@test.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not an assigned Reviewer");
    }

    @Test
    void review_throwsWhenNoteIsBlank() {
        employee.setWorkflowStatus(WorkflowStatus.IN_REVIEW);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> workflowService.review(1L, decisionDto("APPROVE", "   "), "reviewer@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("note is required");
    }

    @Test
    void review_throwsWhenNoteIsNull() {
        employee.setWorkflowStatus(WorkflowStatus.IN_REVIEW);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> workflowService.review(1L, decisionDto("APPROVE", null), "reviewer@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("note is required");
    }

    @Test
    void review_throwsWhenDecisionIsInvalid() {
        employee.setWorkflowStatus(WorkflowStatus.IN_REVIEW);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> workflowService.review(1L, decisionDto("HOLD", "ok"), "reviewer@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("APPROVE or REJECT");
    }

    // ── approve ───────────────────────────────────────────────────────────────

    @Test
    void approve_approve_changesStatusToApproved() {
        employee.setWorkflowStatus(WorkflowStatus.IN_APPROVAL);
        WorkflowDecisionRequestDto dto = decisionDto("APPROVE", "Final OK");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findByEmail("approver@test.com")).thenReturn(Optional.of(approver));
        when(workflowActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(employeeRepository.save(any())).thenReturn(employee);
        when(employeeService.getById(1L)).thenReturn(stubResponse);

        workflowService.approve(1L, dto, "approver@test.com");

        assertThat(employee.getWorkflowStatus()).isEqualTo(WorkflowStatus.APPROVED);
    }

    @Test
    void approve_reject_changesStatusToRejected() {
        employee.setWorkflowStatus(WorkflowStatus.IN_APPROVAL);
        WorkflowDecisionRequestDto dto = decisionDto("REJECT", "Not ready");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findByEmail("approver@test.com")).thenReturn(Optional.of(approver));
        when(workflowActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(employeeRepository.save(any())).thenReturn(employee);
        when(employeeService.getById(1L)).thenReturn(stubResponse);

        workflowService.approve(1L, dto, "approver@test.com");

        assertThat(employee.getWorkflowStatus()).isEqualTo(WorkflowStatus.REJECTED);
    }

    @Test
    void approve_throwsWhenEmployeeNotInApproval() {
        employee.setWorkflowStatus(WorkflowStatus.IN_REVIEW);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> workflowService.approve(1L, decisionDto("APPROVE", "ok"), "approver@test.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not in IN_APPROVAL status");
    }

    @Test
    void approve_throwsWhenApproverNotAssigned() {
        employee.setWorkflowStatus(WorkflowStatus.IN_APPROVAL);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> workflowService.approve(1L, decisionDto("APPROVE", "ok"), "notmine@test.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not an assigned Approver");
    }

    @Test
    void approve_throwsWhenNoteIsBlank() {
        employee.setWorkflowStatus(WorkflowStatus.IN_APPROVAL);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> workflowService.approve(1L, decisionDto("APPROVE", ""), "approver@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("note is required");
    }

    @Test
    void approve_throwsWhenDecisionIsInvalid() {
        employee.setWorkflowStatus(WorkflowStatus.IN_APPROVAL);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> workflowService.approve(1L, decisionDto("MAYBE", "ok"), "approver@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("APPROVE or REJECT");
    }

    // ── getPendingReview ──────────────────────────────────────────────────────

    @Test
    void getPendingReview_returnsEmployeesForReviewer() {
        when(employeeRepository.findByReviewerEmailAndStatus("reviewer@test.com", WorkflowStatus.IN_REVIEW))
                .thenReturn(List.of(employee));
        when(employeeService.getById(1L)).thenReturn(stubResponse);

        List<EmployeeResponseDto> result = workflowService.getPendingReview("reviewer@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getPendingReview_returnsEmptyList_whenNoPendingEmployees() {
        when(employeeRepository.findByReviewerEmailAndStatus(any(), any())).thenReturn(List.of());

        List<EmployeeResponseDto> result = workflowService.getPendingReview("reviewer@test.com");

        assertThat(result).isEmpty();
    }

    // ── getPendingApproval ────────────────────────────────────────────────────

    @Test
    void getPendingApproval_returnsEmployeesForApprover() {
        when(employeeRepository.findByApproverEmailAndStatus("approver@test.com", WorkflowStatus.IN_APPROVAL))
                .thenReturn(List.of(employee));
        when(employeeService.getById(1L)).thenReturn(stubResponse);

        List<EmployeeResponseDto> result = workflowService.getPendingApproval("approver@test.com");

        assertThat(result).hasSize(1);
    }

    @Test
    void getPendingApproval_returnsEmptyList_whenNoPendingEmployees() {
        when(employeeRepository.findByApproverEmailAndStatus(any(), any())).thenReturn(List.of());

        assertThat(workflowService.getPendingApproval("approver@test.com")).isEmpty();
    }

    // ── getWorkflowHistory ────────────────────────────────────────────────────

    @Test
    void getWorkflowHistory_returnsMappedDtos() {
        WorkflowAction action = WorkflowAction.builder()
                .id(1L)
                .employee(employee)
                .performedBy(admin)
                .actionType(WorkflowActionType.SUBMITTED)
                .note("Submitted for review")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(workflowActionRepository.findByEmployeeIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(action));

        List<WorkflowActionDto> result = workflowService.getWorkflowHistory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActionType()).isEqualTo(WorkflowActionType.SUBMITTED);
        assertThat(result.get(0).getNote()).isEqualTo("Submitted for review");
        assertThat(result.get(0).getPerformedByEmail()).isEqualTo("admin@test.com");
    }

    @Test
    void getWorkflowHistory_handlesNullPerformedBy() {
        WorkflowAction action = WorkflowAction.builder()
                .id(2L)
                .employee(employee)
                .performedBy(null)
                .actionType(WorkflowActionType.SUBMITTED)
                .note("System action")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(workflowActionRepository.findByEmployeeIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(action));

        List<WorkflowActionDto> result = workflowService.getWorkflowHistory(1L);

        assertThat(result.get(0).getPerformedByName()).isEqualTo("System");
        assertThat(result.get(0).getPerformedByEmail()).isEqualTo("");
    }

    @Test
    void getWorkflowHistory_throwsWhenEmployeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workflowService.getWorkflowHistory(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private WorkflowDecisionRequestDto decisionDto(String decision, String note) {
        WorkflowDecisionRequestDto dto = new WorkflowDecisionRequestDto();
        dto.setDecision(decision);
        dto.setNote(note);
        return dto;
    }
}
