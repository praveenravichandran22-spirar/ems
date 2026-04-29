package com.minifullstack.ems.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public String store(MultipartFile file, String subDir) throws IOException {
        Path dir = Paths.get(uploadDir, subDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String originalName = file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_")
                : "file";
        String storedName = UUID.randomUUID() + "_" + originalName;

        Files.copy(file.getInputStream(), dir.resolve(storedName));
        return subDir + "/" + storedName;
    }

    public void delete(String relativePath) {
        if (relativePath == null) return;
        try {
            Path path = Paths.get(uploadDir, relativePath).toAbsolutePath().normalize();
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}
