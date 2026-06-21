package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.request.LoginRequestDto;
import com.minifullstack.ems.dto.request.RefreshTokenRequestDto;
import com.minifullstack.ems.dto.request.RegisterRequestDto;
import com.minifullstack.ems.dto.response.AuthResponseDto;
import com.minifullstack.ems.entity.RefreshToken;
import com.minifullstack.ems.entity.User;
import com.minifullstack.ems.enums.Role;
import com.minifullstack.ems.repository.UserRepository;
import com.minifullstack.ems.security.JwtUtils;
import com.minifullstack.ems.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RefreshTokenService refreshTokenService;

    @InjectMocks private AuthServiceImpl authService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("Alice").lastName("Smith")
                .email("alice@test.com")
                .password("encoded")
                .role(Role.ROLE_USER)
                .build();

        refreshToken = RefreshToken.builder()
                .id(1L).user(user)
                .token("refresh-abc")
                .expiryDate(Instant.parse("2030-01-01T00:00:00Z"))
                .build();
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void register_createsUserAndReturnsTokens() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setFirstName("Alice"); dto.setLastName("Smith");
        dto.setEmail("alice@test.com"); dto.setPassword("pass");
        dto.setRole(Role.ROLE_USER);

        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(user);
        when(refreshTokenService.create(any())).thenReturn(refreshToken);
        when(jwtUtils.generateToken(any())).thenReturn("access-token");

        AuthResponseDto response = authService.register(dto);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-abc");
        assertThat(response.getEmail()).isEqualTo("alice@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("alice@test.com");
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void register_defaultsToRoleUser_whenRoleIsNull() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setFirstName("Bob"); dto.setLastName("Jones");
        dto.setEmail("bob@test.com"); dto.setPassword("pass");
        dto.setRole(null);

        when(userRepository.existsByEmail("bob@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            assertThat(u.getRole()).isEqualTo(Role.ROLE_USER);
            return u;
        });
        when(refreshTokenService.create(any())).thenReturn(refreshToken);
        when(jwtUtils.generateToken(any())).thenReturn("token");

        authService.register(dto);
        verify(userRepository).save(any(User.class));
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_authenticatesAndReturnsTokens() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("alice@test.com"); dto.setPassword("pass");

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(refreshTokenService.create(user)).thenReturn(refreshToken);
        when(jwtUtils.generateToken(user)).thenReturn("access-token");

        AuthResponseDto response = authService.login(dto);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    void login_throwsWhenUserNotFoundAfterAuth() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("ghost@test.com"); dto.setPassword("pass");

        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    // ── refresh ───────────────────────────────────────────────────────────────

    @Test
    void refresh_rotatesRefreshTokenAndReturnsNewPair() {
        RefreshTokenRequestDto dto = new RefreshTokenRequestDto();
        dto.setRefreshToken("old-refresh");

        RefreshToken newToken = RefreshToken.builder()
                .token("new-refresh").user(user).expiryDate(Instant.parse("2030-01-01T00:00:00Z")).build();

        when(refreshTokenService.verify("old-refresh")).thenReturn(refreshToken);
        when(refreshTokenService.create(user)).thenReturn(newToken);
        when(jwtUtils.generateToken(user)).thenReturn("new-access");

        AuthResponseDto response = authService.refresh(dto);

        verify(refreshTokenService).deleteByUser(user);
        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    void logout_deletesRefreshToken() {
        RefreshTokenRequestDto dto = new RefreshTokenRequestDto();
        dto.setRefreshToken("refresh-abc");

        when(refreshTokenService.verify("refresh-abc")).thenReturn(refreshToken);

        authService.logout(dto);

        verify(refreshTokenService).deleteByUser(user);
    }
}
