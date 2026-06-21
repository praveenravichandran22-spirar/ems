package com.minifullstack.ems.controller;

import com.minifullstack.ems.dto.request.CreateUserRequestDto;
import com.minifullstack.ems.dto.request.UpdateUserRequestDto;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.UserResponseDto;
import com.minifullstack.ems.enums.Role;
import com.minifullstack.ems.service.UserManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserManagementService userManagementService;

    @InjectMocks private UserController userController;

    private UserResponseDto userDto(Long id, Role role) {
        return UserResponseDto.builder().id(id).firstName("Test").lastName("User")
                .email("user@test.com").role(role).build();
    }

    @Test
    void search_returns200WithPagedResponse() {
        PagedResponse<UserResponseDto> page = PagedResponse.<UserResponseDto>builder()
                .content(List.of(userDto(1L, Role.ROLE_REVIEWER)))
                .totalElements(1).page(0).size(10).totalPages(1).last(true).build();

        when(userManagementService.search(any(), any(), eq(0), eq(10), any(), any()))
                .thenReturn(page);

        ResponseEntity<PagedResponse<UserResponseDto>> response =
                userController.search(null, null, 0, 10, "firstName", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void createUser_returns201WithCreatedUser() {
        when(userManagementService.createUser(any())).thenReturn(userDto(2L, Role.ROLE_REVIEWER));

        ResponseEntity<UserResponseDto> response = userController.createUser(new CreateUserRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getRole()).isEqualTo(Role.ROLE_REVIEWER);
    }

    @Test
    void updateUser_returns200WithUpdatedUser() {
        when(userManagementService.updateUser(eq(1L), any())).thenReturn(userDto(1L, Role.ROLE_APPROVER));

        ResponseEntity<UserResponseDto> response = userController.updateUser(1L, new UpdateUserRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getRole()).isEqualTo(Role.ROLE_APPROVER);
    }

    @Test
    void deleteUser_returns204AndDeletesUser() {
        doNothing().when(userManagementService).deleteUser(1L);

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userManagementService).deleteUser(1L);
    }

    @Test
    void listReviewers_returns200WithReviewerList() {
        when(userManagementService.listByRole(Role.ROLE_REVIEWER))
                .thenReturn(List.of(userDto(1L, Role.ROLE_REVIEWER)));

        ResponseEntity<List<UserResponseDto>> response = userController.listReviewers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getRole()).isEqualTo(Role.ROLE_REVIEWER);
    }

    @Test
    void listApprovers_returns200WithApproverList() {
        when(userManagementService.listByRole(Role.ROLE_APPROVER))
                .thenReturn(List.of(userDto(2L, Role.ROLE_APPROVER)));

        ResponseEntity<List<UserResponseDto>> response = userController.listApprovers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get(0).getRole()).isEqualTo(Role.ROLE_APPROVER);
    }
}
