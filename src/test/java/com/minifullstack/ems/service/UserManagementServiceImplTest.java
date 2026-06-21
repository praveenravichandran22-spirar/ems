package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.request.CreateUserRequestDto;
import com.minifullstack.ems.dto.request.UpdateUserRequestDto;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.UserResponseDto;
import com.minifullstack.ems.entity.User;
import com.minifullstack.ems.enums.Role;
import com.minifullstack.ems.repository.UserRepository;
import com.minifullstack.ems.service.impl.UserManagementServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserManagementServiceImpl userService;

    private User reviewer;
    private User approver;

    @BeforeEach
    void setUp() {
        reviewer = User.builder().id(1L).firstName("Rev").lastName("User")
                .email("reviewer@test.com").password("encoded").role(Role.ROLE_REVIEWER).build();
        approver = User.builder().id(2L).firstName("App").lastName("User")
                .email("approver@test.com").password("encoded").role(Role.ROLE_APPROVER).build();
    }

    // ── search ────────────────────────────────────────────────────────────────

    @Test
    void search_withoutRoleFilter_returnsPagedResult() {
        Page<User> page = new PageImpl<>(List.of(reviewer, approver));
        when(userRepository.searchAll(anyString(), any(Pageable.class))).thenReturn(page);

        PagedResponse<UserResponseDto> result = userService.search(null, null, 0, 10, "id", "asc");

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void search_withRoleFilter_usesRoleSearch() {
        Page<User> page = new PageImpl<>(List.of(reviewer));
        when(userRepository.searchByRole(anyString(), eq(Role.ROLE_REVIEWER), any(Pageable.class)))
                .thenReturn(page);

        PagedResponse<UserResponseDto> result =
                userService.search("rev", "ROLE_REVIEWER", 0, 10, "id", "asc");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("reviewer@test.com");
        verify(userRepository).searchByRole(anyString(), eq(Role.ROLE_REVIEWER), any(Pageable.class));
    }

    @Test
    void search_withDescSort_appliesDescendingOrder() {
        Page<User> page = new PageImpl<>(List.of(reviewer));
        when(userRepository.searchAll(anyString(), any(Pageable.class))).thenReturn(page);

        PagedResponse<UserResponseDto> result = userService.search("", null, 0, 10, "id", "desc");

        assertThat(result.getContent()).hasSize(1);
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_savesAndReturnsDto() {
        CreateUserRequestDto dto = new CreateUserRequestDto();
        dto.setFirstName("Rev"); dto.setLastName("User");
        dto.setEmail("reviewer@test.com"); dto.setPassword("pass");
        dto.setRole(Role.ROLE_REVIEWER);

        when(userRepository.existsByEmail("reviewer@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(reviewer);

        UserResponseDto result = userService.createUser(dto);

        assertThat(result.getEmail()).isEqualTo("reviewer@test.com");
        assertThat(result.getRole()).isEqualTo(Role.ROLE_REVIEWER);
    }

    @Test
    void createUser_throwsWhenEmailAlreadyExists() {
        CreateUserRequestDto dto = new CreateUserRequestDto();
        dto.setEmail("reviewer@test.com");
        dto.setRole(Role.ROLE_REVIEWER);

        when(userRepository.existsByEmail("reviewer@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void createUser_throwsWhenRoleIsNull() {
        CreateUserRequestDto dto = new CreateUserRequestDto();
        dto.setEmail("x@test.com"); dto.setRole(null);

        when(userRepository.existsByEmail("x@test.com")).thenReturn(false);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ROLE_REVIEWER or ROLE_APPROVER");
    }

    @Test
    void createUser_throwsWhenRoleIsAdmin() {
        CreateUserRequestDto dto = new CreateUserRequestDto();
        dto.setEmail("x@test.com"); dto.setRole(Role.ROLE_ADMIN);

        when(userRepository.existsByEmail("x@test.com")).thenReturn(false);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ROLE_REVIEWER or ROLE_APPROVER");
    }

    // ── updateUser ────────────────────────────────────────────────────────────

    @Test
    void updateUser_updatesFieldsAndReturnsDto() {
        UpdateUserRequestDto dto = new UpdateUserRequestDto();
        dto.setFirstName("Updated"); dto.setLastName("Name");
        dto.setRole(Role.ROLE_APPROVER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(reviewer));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.updateUser(1L, dto);

        assertThat(reviewer.getFirstName()).isEqualTo("Updated");
        assertThat(reviewer.getRole()).isEqualTo(Role.ROLE_APPROVER);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUser_encodesPassword_whenPasswordProvided() {
        UpdateUserRequestDto dto = new UpdateUserRequestDto();
        dto.setFirstName("Rev"); dto.setLastName("User");
        dto.setRole(Role.ROLE_REVIEWER); dto.setPassword("newpass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(reviewer));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded-new");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.updateUser(1L, dto);

        assertThat(reviewer.getPassword()).isEqualTo("encoded-new");
        verify(passwordEncoder).encode("newpass");
    }

    @Test
    void updateUser_throwsWhenUserNotFound() {
        UpdateUserRequestDto dto = new UpdateUserRequestDto();
        dto.setRole(Role.ROLE_REVIEWER);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateUser_throwsWhenRoleIsAdmin() {
        UpdateUserRequestDto dto = new UpdateUserRequestDto();
        dto.setRole(Role.ROLE_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(reviewer));

        assertThatThrownBy(() -> userService.updateUser(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ROLE_REVIEWER or ROLE_APPROVER");
    }

    // ── deleteUser ────────────────────────────────────────────────────────────

    @Test
    void deleteUser_deletesExistingUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_throwsWhenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── listByRole ────────────────────────────────────────────────────────────

    @Test
    void listByRole_returnsUsersWithGivenRole() {
        when(userRepository.findAllByRole(Role.ROLE_REVIEWER)).thenReturn(List.of(reviewer));

        List<UserResponseDto> result = userService.listByRole(Role.ROLE_REVIEWER);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(Role.ROLE_REVIEWER);
    }

    @Test
    void listByRole_returnsEmptyListWhenNoneFound() {
        when(userRepository.findAllByRole(Role.ROLE_APPROVER)).thenReturn(List.of());

        List<UserResponseDto> result = userService.listByRole(Role.ROLE_APPROVER);

        assertThat(result).isEmpty();
    }
}
