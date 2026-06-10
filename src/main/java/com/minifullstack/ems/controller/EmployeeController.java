package com.minifullstack.ems.controller;

import com.minifullstack.ems.dto.request.AssignWorkflowRequestDto;
import com.minifullstack.ems.dto.request.EmployeeRequestDto;
import com.minifullstack.ems.dto.request.WorkflowDecisionRequestDto;
import com.minifullstack.ems.dto.response.EmployeeResponseDto;
import com.minifullstack.ems.dto.response.FileResponse;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.WorkflowActionDto;
import com.minifullstack.ems.service.EmployeeService;
import com.minifullstack.ems.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final WorkflowService workflowService;

    // ── Create ───────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<EmployeeResponseDto> create(@RequestBody EmployeeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(dto));
    }

    // ── Get by ID ────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    // ── List all (paginated + sortable) ──────────────────────────────────────
    @GetMapping
    public ResponseEntity<PagedResponse<EmployeeResponseDto>> getAll(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(employeeService.getAll(page, size, sortBy, sortDir));
    }

    // ── Search / filter (paginated + sortable) ───────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<EmployeeResponseDto>> search(
            @RequestParam(required = false)     String keyword,
            @RequestParam(required = false)     Long departmentId,
            @RequestParam(required = false)     Long statusId,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(
                employeeService.search(keyword, departmentId, statusId, page, size, sortBy, sortDir));
    }

    // ── Update ───────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> update(
            @PathVariable Long id,
            @RequestBody EmployeeRequestDto dto) {
        return ResponseEntity.ok(employeeService.update(id, dto));
    }

    // ── Delete ───────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Upload profile image ──────────────────────────────────────────────────
    @PostMapping("/{id}/profile-image")
    public ResponseEntity<EmployeeResponseDto> uploadProfileImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(employeeService.uploadProfileImage(id, file));
    }

    // ── Upload resume ─────────────────────────────────────────────────────────
    @PostMapping("/{id}/resume")
    public ResponseEntity<EmployeeResponseDto> uploadResume(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(employeeService.uploadResume(id, file));
    }

    // ── Remove profile image ──────────────────────────────────────────────────
    @DeleteMapping("/{id}/profile-image")
    public ResponseEntity<EmployeeResponseDto> removeProfileImage(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.removeProfileImage(id));
    }

    // ── Remove resume ─────────────────────────────────────────────────────────
    @DeleteMapping("/{id}/resume")
    public ResponseEntity<EmployeeResponseDto> removeResume(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.removeResume(id));
    }

    // ── Serve profile image from DB ───────────────────────────────────────────
    @GetMapping("/{id}/profile-image")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long id) {
        FileResponse file = employeeService.getProfileImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.data());
    }

    // ── Serve resume from DB ──────────────────────────────────────────────────
    @GetMapping("/{id}/resume")
    public ResponseEntity<byte[]> getResume(@PathVariable Long id) {
        FileResponse file = employeeService.getResume(id);
        String fileName = file.fileName() != null ? file.fileName() : "resume";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .body(file.data());
    }

    // ── Workflow: Admin assigns reviewers + approvers ─────────────────────────
    @PutMapping("/{id}/assign-workflow")
    public ResponseEntity<EmployeeResponseDto> assignWorkflow(
            @PathVariable Long id,
            @RequestBody AssignWorkflowRequestDto dto) {
        return ResponseEntity.ok(workflowService.assignWorkflow(id, dto));
    }

    // ── Workflow: Admin submits DRAFT/REJECTED → IN_REVIEW ────────────────────
    @PostMapping("/{id}/submit")
    public ResponseEntity<EmployeeResponseDto> submit(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workflowService.submit(id, userDetails.getUsername()));
    }

    // ── Workflow: Reviewer queue ──────────────────────────────────────────────
    @GetMapping("/pending-review")
    public ResponseEntity<List<EmployeeResponseDto>> pendingReview(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workflowService.getPendingReview(userDetails.getUsername()));
    }

    // ── Workflow: Reviewer approves or rejects ────────────────────────────────
    @PostMapping("/{id}/review")
    public ResponseEntity<EmployeeResponseDto> review(
            @PathVariable Long id,
            @RequestBody WorkflowDecisionRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workflowService.review(id, dto, userDetails.getUsername()));
    }

    // ── Workflow: Approver queue ──────────────────────────────────────────────
    @GetMapping("/pending-approval")
    public ResponseEntity<List<EmployeeResponseDto>> pendingApproval(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workflowService.getPendingApproval(userDetails.getUsername()));
    }

    // ── Workflow: Approver approves or rejects ────────────────────────────────
    @PostMapping("/{id}/approve")
    public ResponseEntity<EmployeeResponseDto> approve(
            @PathVariable Long id,
            @RequestBody WorkflowDecisionRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workflowService.approve(id, dto, userDetails.getUsername()));
    }

    // ── Workflow: Full action history ─────────────────────────────────────────
    @GetMapping("/{id}/workflow-history")
    public ResponseEntity<List<WorkflowActionDto>> workflowHistory(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.getWorkflowHistory(id));
    }
}
