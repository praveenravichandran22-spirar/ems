package com.minifullstack.ems.service.impl;

import com.minifullstack.ems.dto.request.CreateUserRequestDto;
import com.minifullstack.ems.dto.request.UpdateUserRequestDto;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.UserResponseDto;
import com.minifullstack.ems.entity.User;
import com.minifullstack.ems.enums.Role;
import com.minifullstack.ems.repository.UserRepository;
import com.minifullstack.ems.service.UserManagementService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PagedResponse<UserResponseDto> search(
            String keyword, String role,
            int page, int size, String sortBy, String sortDir) {

        String search = (keyword != null && !keyword.isBlank()) ? keyword.toLowerCase() : "";

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> result;
        if (role != null && !role.isBlank()) {
            Role roleEnum = Role.valueOf(role);
            result = userRepository.searchByRole(search, roleEnum, pageable);
        } else {
            result = userRepository.searchAll(search, pageable);
        }

        return PagedResponse.<UserResponseDto>builder()
                .content(result.getContent().stream().map(this::toDto).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    @Override
    public UserResponseDto createUser(CreateUserRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }
        if (dto.getRole() == null || dto.getRole() == Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("Role must be ROLE_REVIEWER or ROLE_APPROVER");
        }
        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();
        return toDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto updateUser(Long id, UpdateUserRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        if (dto.getRole() == null || dto.getRole() == Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("Role must be ROLE_REVIEWER or ROLE_APPROVER");
        }
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(dto.getRole());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        return toDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<UserResponseDto> listByRole(Role role) {
        return userRepository.findAllByRole(role).stream().map(this::toDto).toList();
    }

    private UserResponseDto toDto(User u) {
        return UserResponseDto.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .role(u.getRole())
                .enabled(u.isEnabled())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
