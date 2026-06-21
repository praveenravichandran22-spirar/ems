package com.minifullstack.ems.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ── ResourceNotFoundException → 404 ──────────────────────────────────────

    @Test
    void handleNotFound_returns404WithMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleNotFound(new ResourceNotFoundException("Employee not found with id: 99"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", 404);
        assertThat(response.getBody().get("error").toString()).contains("Employee not found");
    }

    @Test
    void handleNotFound_bodyContainsTimestamp() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleNotFound(new ResourceNotFoundException("Not found"));

        assertThat(response.getBody()).containsKey("timestamp");
        assertThat(response.getBody().get("timestamp").toString()).isNotBlank();
    }

    // ── IllegalArgumentException → 400 ───────────────────────────────────────

    @Test
    void handleBadRequest_returns400WithMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleBadRequest(new IllegalArgumentException("Email already registered"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat(response.getBody().get("error").toString()).contains("Email already registered");
    }

    @Test
    void handleBadRequest_bodyContainsTimestamp() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleBadRequest(new IllegalArgumentException("Bad input"));

        assertThat(response.getBody()).containsKey("timestamp");
    }

    // ── TokenRefreshException → 401 ──────────────────────────────────────────

    @Test
    void handleTokenRefresh_returns401WithMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleTokenRefresh(new TokenRefreshException("Refresh token has expired"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("status", 401);
        assertThat(response.getBody().get("error").toString()).contains("expired");
    }

    @Test
    void handleTokenRefresh_returnsTimestampInBody() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleTokenRefresh(new TokenRefreshException("Token not found"));

        assertThat(response.getBody()).containsKey("timestamp");
    }

    // ── Generic Exception → 500 ───────────────────────────────────────────────

    @Test
    void handleGeneric_returns500WithMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneric(new RuntimeException("Something went wrong"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", 500);
        assertThat(response.getBody().get("error").toString()).contains("Something went wrong");
    }

    @Test
    void handleGeneric_returnsTimestampInBody() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneric(new Exception("Unexpected"));

        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    void handleGeneric_prefixesMessageWithUnexpectedError() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneric(new Exception("database down"));

        assertThat(response.getBody().get("error").toString())
                .startsWith("An unexpected error occurred:");
    }
}
