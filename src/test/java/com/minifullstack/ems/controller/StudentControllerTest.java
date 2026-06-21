package com.minifullstack.ems.controller;

import com.minifullstack.ems.dto.request.StudentRequestDto;
import com.minifullstack.ems.dto.response.FileResponse;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.StudentResponseDto;
import com.minifullstack.ems.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock private StudentService studentService;

    @InjectMocks private StudentController studentController;

    private StudentResponseDto stubStudent;

    @BeforeEach
    void setUp() {
        stubStudent = StudentResponseDto.builder()
                .id(1L).firstName("Alice").lastName("Brown")
                .email("alice@student.com").enrollmentNumber("EN001")
                .course("Computer Science").isActive(true).build();
    }

    @Test
    void create_returns201WithStudent() {
        when(studentService.create(any())).thenReturn(stubStudent);

        ResponseEntity<StudentResponseDto> response = studentController.create(new StudentRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getEmail()).isEqualTo("alice@student.com");
    }

    @Test
    void getById_returns200WithStudent() {
        when(studentService.getById(1L)).thenReturn(stubStudent);

        ResponseEntity<StudentResponseDto> response = studentController.getById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEnrollmentNumber()).isEqualTo("EN001");
    }

    @Test
    void search_returns200WithPagedResponse() {
        PagedResponse<StudentResponseDto> page = PagedResponse.<StudentResponseDto>builder()
                .content(List.of(stubStudent)).totalElements(1).page(0).size(10).totalPages(1).last(true).build();
        when(studentService.search(any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(page);

        ResponseEntity<PagedResponse<StudentResponseDto>> response =
                studentController.search("alice", null, null, null, 0, 10, "id", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void update_returns200WithUpdatedStudent() {
        when(studentService.update(eq(1L), any())).thenReturn(stubStudent);

        ResponseEntity<StudentResponseDto> response =
                studentController.update(1L, new StudentRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void delete_returns204() {
        doNothing().when(studentService).delete(1L);

        ResponseEntity<Void> response = studentController.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(studentService).delete(1L);
    }

    @Test
    void uploadProfileImage_returns200WithStudent() {
        MultipartFile file = mock(MultipartFile.class);
        when(studentService.uploadProfileImage(eq(1L), any())).thenReturn(stubStudent);

        ResponseEntity<StudentResponseDto> response = studentController.uploadProfileImage(1L, file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(studentService).uploadProfileImage(1L, file);
    }

    @Test
    void getProfileImage_returns200WithImageBytes() {
        byte[] imageBytes = new byte[]{1, 2, 3};
        FileResponse fileResponse = new FileResponse(imageBytes, "image/jpeg", null);
        when(studentService.getProfileImage(1L)).thenReturn(fileResponse);

        ResponseEntity<byte[]> response = studentController.getProfileImage(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(imageBytes);
    }
}
