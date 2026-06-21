package com.minifullstack.ems.controller;

import com.minifullstack.ems.dto.request.LoginRequestDto;
import com.minifullstack.ems.dto.request.RefreshTokenRequestDto;
import com.minifullstack.ems.dto.request.RegisterRequestDto;
import com.minifullstack.ems.dto.response.AuthResponseDto;
import com.minifullstack.ems.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@RequestBody RefreshTokenRequestDto dto) {
        return ResponseEntity.ok(authService.refresh(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequestDto dto) {
        authService.logout(dto);
        return ResponseEntity.noContent().build();
    }
}
