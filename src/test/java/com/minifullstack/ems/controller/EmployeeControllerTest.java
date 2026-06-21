package com.minifullstack.ems.controller;

import com.minifullstack.ems.dto.request.AssignWorkflowRequestDto;
import com.minifullstack.ems.dto.request.EmployeeRequestDto;
import com.minifullstack.ems.dto.request.WorkflowDecisionRequestDto;
import com.minifullstack.ems.dto.response.EmployeeResponseDto;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.WorkflowActionDto;
import com.minifullstack.ems.enums.WorkflowStatus;
import com.minifullstack.ems.service.EmployeeService;
import com.minifullstack.ems.service.WorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock private EmployeeService employeeService;
    @Mock private WorkflowService workflowService;

    @InjectMocks private EmployeeController employeeController;

    private EmployeeResponseDto stubEmployee;
    private UserDetails adminUser;
    private UserDetails reviewerUser;
    private UserDetails approverUser;

    @BeforeEach
    void setUp() {
        stubEmployee = EmployeeResponseDto.builder()
                .id(1L).firstName("John").lastName("Doe")
                .email("john@test.com").workflowStatus(WorkflowStatus.DRAFT)
                .assignedReviewers(List.of()).assignedApprovers(List.of())
                .build();

        adminUser    = User.builder().username("admin@test.com").password("p").authorities("ROLE_ADMIN").build();
        reviewerUser = User.builder().username("reviewer@test.com").password("p").authorities("ROLE_REVIEWER").build();
        approverUser = User.builder().username("approver@test.com").password("p").authorities("ROLE_APPROVER").build();
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @Test
    void create_returns201WithEmployee() {
        when(employeeService.create(any())).thenReturn(stubEmployee);

        ResponseEntity<EmployeeResponseDto> response = employeeController.create(new EmployeeRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void getById_returns200WithEmployee() {
        when(employeeService.getById(1L)).thenReturn(stubEmployee);

        ResponseEntity<EmployeeResponseDto> response = employeeController.getById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void getAll_returns200WithPagedResponse() {
        PagedResponse<EmployeeResponseDto> page = PagedResponse.<EmployeeResponseDto>builder()
                .content(List.of(stubEmployee)).totalElements(1).page(0).size(10).totalPages(1).last(true).build();
        when(employeeService.getAll(0, 10, "id", "asc")).thenReturn(page);

        ResponseEntity<PagedResponse<EmployeeResponseDto>> response =
                employeeController.getAll(0, 10, "id", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void search_returns200WithMatchingEmployees() {
        PagedResponse<EmployeeResponseDto> page = PagedResponse.<EmployeeResponseDto>builder()
                .content(List.of(stubEmployee)).totalElements(1).page(0).size(10).totalPages(1).last(true).build();
        when(employeeService.search(any(), any(), any(), anyInt(), anyInt(), any(), any())).thenReturn(page);

        ResponseEntity<PagedResponse<EmployeeResponseDto>> response =
                employeeController.search("john", null, null, 0, 10, "id", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void update_returns200WithUpdatedEmployee() {
        when(employeeService.update(eq(1L), any())).thenReturn(stubEmployee);

        ResponseEntity<EmployeeResponseDto> response = employeeController.update(1L, new EmployeeRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getFirstName()).isEqualTo("John");
    }

    @Test
    void delete_returns204() {
        doNothing().when(employeeService).delete(1L);

        ResponseEntity<Void> response = employeeController.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(employeeService).delete(1L);
    }

    // ── Workflow: Admin ───────────────────────────────────────────────────────

    @Test
    void assignWorkflow_returns200() {
        when(workflowService.assignWorkflow(eq(1L), any())).thenReturn(stubEmployee);

        ResponseEntity<EmployeeResponseDto> response =
                employeeController.assignWorkflow(1L, new AssignWorkflowRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(workflowService).assignWorkflow(eq(1L), any());
    }

    @Test
    void submit_returns200WithInReviewEmployee() {
        EmployeeResponseDto inReview = EmployeeResponseDto.builder()
                .id(1L).workflowStatus(WorkflowStatus.IN_REVIEW)
                .assignedReviewers(List.of()).assignedApprovers(List.of()).build();
        when(workflowService.submit(1L, "admin@test.com")).thenReturn(inReview);

        ResponseEntity<EmployeeResponseDto> response = employeeController.submit(1L, adminUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getWorkflowStatus()).isEqualTo(WorkflowStatus.IN_REVIEW);
    }

    // ── Workflow: Reviewer ────────────────────────────────────────────────────

    @Test
    void pendingReview_returns200WithList() {
        when(workflowService.getPendingReview("reviewer@test.com")).thenReturn(List.of(stubEmployee));

        ResponseEntity<List<EmployeeResponseDto>> response =
                employeeController.pendingReview(reviewerUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void review_returns200AfterDecision() {
        WorkflowDecisionRequestDto dto = new WorkflowDecisionRequestDto();
        dto.setDecision("APPROVE"); dto.setNote("Looks good");

        EmployeeResponseDto inApproval = EmployeeResponseDto.builder()
                .id(1L).workflowStatus(WorkflowStatus.IN_APPROVAL)
                .assignedReviewers(List.of()).assignedApprovers(List.of()).build();
        when(workflowService.review(1L, dto, "reviewer@test.com")).thenReturn(inApproval);

        ResponseEntity<EmployeeResponseDto> response = employeeController.review(1L, dto, reviewerUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getWorkflowStatus()).isEqualTo(WorkflowStatus.IN_APPROVAL);
    }

    // ── Workflow: Approver ────────────────────────────────────────────────────

    @Test
    void pendingApproval_returns200WithList() {
        when(workflowService.getPendingApproval("approver@test.com")).thenReturn(List.of(stubEmployee));

        ResponseEntity<List<EmployeeResponseDto>> response =
                employeeController.pendingApproval(approverUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void approve_returns200AfterDecision() {
        WorkflowDecisionRequestDto dto = new WorkflowDecisionRequestDto();
        dto.setDecision("APPROVE"); dto.setNote("Final approval");

        EmployeeResponseDto approved = EmployeeResponseDto.builder()
                .id(1L).workflowStatus(WorkflowStatus.APPROVED)
                .assignedReviewers(List.of()).assignedApprovers(List.of()).build();
        when(workflowService.approve(1L, dto, "approver@test.com")).thenReturn(approved);

        ResponseEntity<EmployeeResponseDto> response = employeeController.approve(1L, dto, approverUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getWorkflowStatus()).isEqualTo(WorkflowStatus.APPROVED);
    }

    // ── Workflow: History ─────────────────────────────────────────────────────

    @Test
    void workflowHistory_returns200WithActions() {
        WorkflowActionDto action = WorkflowActionDto.builder().id(1L).note("Submitted").build();
        when(workflowService.getWorkflowHistory(1L)).thenReturn(List.of(action));

        ResponseEntity<List<WorkflowActionDto>> response = employeeController.workflowHistory(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }
}
