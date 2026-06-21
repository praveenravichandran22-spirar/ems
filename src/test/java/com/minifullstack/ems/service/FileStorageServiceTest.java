package com.minifullstack.ems.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService svc;

    @BeforeEach
    void setUp() {
        svc = new FileStorageService();
        ReflectionTestUtils.setField(svc, "uploadDir", tempDir.toString());
    }

    // ── store ─────────────────────────────────────────────────────────────────

    @Test
    void store_createsFileAndReturnsRelativePath() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg",
                "image/jpeg", "data".getBytes());

        String result = svc.store(file, "employees");

        assertThat(result).startsWith("employees/").endsWith("photo.jpg");
        Path stored = tempDir.resolve(result);
        assertThat(Files.exists(stored)).isTrue();
    }

    @Test
    void store_sanitisesFilename() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "../evil.sh",
                "text/plain", "x".getBytes());

        String result = svc.store(file, "employees");

        // path separator '/' is replaced; stored file must exist inside tempDir
        assertThat(result).doesNotContain("/evil");
        Path stored = tempDir.resolve(result);
        assertThat(stored).startsWith(tempDir);
        assertThat(Files.exists(stored)).isTrue();
    }

    @Test
    void store_rejectsPathTraversalInSubDir() {
        MockMultipartFile file = new MockMultipartFile("file", "f.txt",
                "text/plain", "x".getBytes());

        assertThatThrownBy(() -> svc.store(file, "../../outside"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Invalid upload subdirectory");
    }

    @Test
    void store_handlesNullOriginalFilename() throws IOException {
        // MockMultipartFile converts null originalFilename to "", so the "file"
        // fallback in FileStorageService is not triggered — just verify storage succeeds.
        MockMultipartFile file = new MockMultipartFile("file", null,
                "application/octet-stream", "bytes".getBytes());

        String result = svc.store(file, "students");

        assertThat(result).startsWith("students/");
        assertThat(Files.exists(tempDir.resolve(result))).isTrue();
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_removesExistingFile() throws IOException {
        Path sub = tempDir.resolve("employees");
        Files.createDirectories(sub);
        Path target = sub.resolve("test.jpg");
        Files.writeString(target, "data");

        svc.delete("employees/test.jpg");

        assertThat(Files.exists(target)).isFalse();
    }

    @Test
    void delete_doesNotThrowForMissingFile() {
        assertThatCode(() -> svc.delete("employees/nonexistent.jpg"))
                .doesNotThrowAnyException();
    }

    @Test
    void delete_doesNotThrowForNullPath() {
        assertThatCode(() -> svc.delete(null)).doesNotThrowAnyException();
    }

    @Test
    void delete_silentlyIgnoresPathTraversal() throws IOException {
        Path sensitive = tempDir.getParent().resolve("sensitive.txt");
        Files.writeString(sensitive, "secret");

        // must NOT delete the file outside the upload dir
        svc.delete("../../sensitive.txt");

        assertThat(Files.exists(sensitive)).isTrue();
        Files.deleteIfExists(sensitive);
    }
}
