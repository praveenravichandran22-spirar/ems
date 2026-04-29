package com.minifullstack.ems.service.impl;

import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.UserResponseDto;
import com.minifullstack.ems.entity.User;
import com.minifullstack.ems.enums.Role;
import com.minifullstack.ems.repository.UserRepository;
import com.minifullstack.ems.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;

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
