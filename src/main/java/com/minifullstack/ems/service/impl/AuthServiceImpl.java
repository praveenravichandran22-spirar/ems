package com.minifullstack.ems.service.impl;

import com.minifullstack.ems.dto.request.LoginRequestDto;
import com.minifullstack.ems.dto.request.RefreshTokenRequestDto;
import com.minifullstack.ems.dto.request.RegisterRequestDto;
import com.minifullstack.ems.dto.response.AuthResponseDto;
import com.minifullstack.ems.entity.RefreshToken;
import com.minifullstack.ems.entity.User;
import com.minifullstack.ems.enums.Role;
import com.minifullstack.ems.repository.UserRepository;
import com.minifullstack.ems.security.JwtUtils;
import com.minifullstack.ems.service.AuthService;
import com.minifullstack.ems.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponseDto register(RegisterRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }

        Role role = dto.getRole() != null ? dto.getRole() : Role.ROLE_USER;

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
        return buildResponse(user);
    }

    @Override
    public AuthResponseDto login(LoginRequestDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return buildResponse(user);
    }

    @Override
    public AuthResponseDto refresh(RefreshTokenRequestDto dto) {
        RefreshToken verified = refreshTokenService.verify(dto.getRefreshToken());
        User user = verified.getUser();

        // Rotate: delete old refresh token, issue new pair
        refreshTokenService.deleteByUser(user);
        return buildResponse(user);
    }

    @Override
    public void logout(RefreshTokenRequestDto dto) {
        RefreshToken verified = refreshTokenService.verify(dto.getRefreshToken());
        refreshTokenService.deleteByUser(verified.getUser());
    }

    private AuthResponseDto buildResponse(User user) {
        RefreshToken refreshToken = refreshTokenService.create(user);
        return AuthResponseDto.builder()
                .accessToken(jwtUtils.generateToken(user))
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
