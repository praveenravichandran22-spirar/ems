package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.request.LoginRequestDto;
import com.minifullstack.ems.dto.request.RefreshTokenRequestDto;
import com.minifullstack.ems.dto.request.RegisterRequestDto;
import com.minifullstack.ems.dto.response.AuthResponseDto;

public interface AuthService {
    AuthResponseDto register(RegisterRequestDto dto);
    AuthResponseDto login(LoginRequestDto dto);
    AuthResponseDto refresh(RefreshTokenRequestDto dto);
    void logout(RefreshTokenRequestDto dto);
}
