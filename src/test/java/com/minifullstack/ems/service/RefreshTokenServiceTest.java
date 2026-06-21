package com.minifullstack.ems.service;

import com.minifullstack.ems.entity.RefreshToken;
import com.minifullstack.ems.entity.User;
import com.minifullstack.ems.enums.Role;
import com.minifullstack.ems.exception.TokenRefreshException;
import com.minifullstack.ems.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", 604_800_000L);

        user = User.builder()
                .id(1L)
                .email("user@test.com")
                .role(Role.ROLE_USER)
                .password("encoded")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_deletesExistingTokenAndSavesNew() {
        RefreshToken saved = RefreshToken.builder()
                .id(1L)
                .user(user)
                .token("new-uuid")
                .expiryDate(Instant.now().plusMillis(604_800_000L))
                .build();
        when(refreshTokenRepository.save(any())).thenReturn(saved);

        RefreshToken result = refreshTokenService.create(user);

        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        assertThat(result.getToken()).isEqualTo("new-uuid");
    }

    @Test
    void create_setsExpiryInFuture() {
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.create(user);

        assertThat(result.getExpiryDate()).isAfter(Instant.now());
    }

    // ── verify ────────────────────────────────────────────────────────────────

    @Test
    void verify_returnsToken_whenValidAndNotExpired() {
        RefreshToken token = RefreshToken.builder()
                .id(1L)
                .user(user)
                .token("valid-token")
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.verify("valid-token");

        assertThat(result).isEqualTo(token);
    }

    @Test
    void verify_throwsTokenRefreshException_whenTokenNotFound() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.verify("missing"))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void verify_throwsAndDeletesToken_whenExpired() {
        RefreshToken expired = RefreshToken.builder()
                .id(2L)
                .user(user)
                .token("expired-token")
                .expiryDate(Instant.now().minusSeconds(3600))
                .build();
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenService.verify("expired-token"))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("expired");

        verify(refreshTokenRepository).delete(expired);
    }

    // ── deleteByUser ──────────────────────────────────────────────────────────

    @Test
    void deleteByUser_delegatesToRepository() {
        refreshTokenService.deleteByUser(user);
        verify(refreshTokenRepository).deleteByUser(user);
    }
}
