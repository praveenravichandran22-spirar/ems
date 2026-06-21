package com.minifullstack.ems.controller;

import com.minifullstack.ems.dto.request.LoginRequestDto;
import com.minifullstack.ems.dto.request.RefreshTokenRequestDto;
import com.minifullstack.ems.dto.request.RegisterRequestDto;
import com.minifullstack.ems.dto.response.AuthResponseDto;
import com.minifullstack.ems.enums.Role;
import com.minifullstack.ems.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;

    @InjectMocks private AuthController authController;

    private AuthResponseDto stubResponse() {
        return AuthResponseDto.builder()
                .accessToken("access-token").refreshToken("refresh-token")
                .email("user@test.com").role(Role.ROLE_USER)
                .firstName("Test").lastName("User")
                .build();
    }

    @Test
    void register_returns201WithAuthResponse() {
        when(authService.register(any())).thenReturn(stubResponse());

        ResponseEntity<AuthResponseDto> response = authController.register(new RegisterRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getAccessToken()).isEqualTo("access-token");
        verify(authService).register(any());
    }

    @Test
    void login_returns200WithAuthResponse() {
        when(authService.login(any())).thenReturn(stubResponse());

        ResponseEntity<AuthResponseDto> response = authController.login(new LoginRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void refresh_returns200WithNewTokens() {
        when(authService.refresh(any())).thenReturn(stubResponse());

        ResponseEntity<AuthResponseDto> response = authController.refresh(new RefreshTokenRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmail()).isEqualTo("user@test.com");
    }

    @Test
    void logout_returns204AndDelegatesLogout() {
        doNothing().when(authService).logout(any());

        ResponseEntity<Void> response = authController.logout(new RefreshTokenRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authService).logout(any());
    }
}
