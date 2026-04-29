package com.minifullstack.ems.dto.response;

public record FileResponse(byte[] data, String contentType, String fileName) {}
