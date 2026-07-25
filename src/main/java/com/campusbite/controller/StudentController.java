package com.campusbite.controller;

import com.campusbite.dto.request.StudentPhoneUpdateRequestDTO;
import com.campusbite.dto.response.StudentResponseDTO;
import java.security.Principal;
import com.campusbite.entity.Student;
import com.campusbite.service.StudentService;
import com.campusbite.mapper.StudentMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;
    private final StudentMapper studentMapper;

    public StudentController(StudentService studentService, StudentMapper studentMapper) {
        this.studentService = studentService;
        this.studentMapper = studentMapper;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/profile")
    public ResponseEntity<StudentResponseDTO> getProfile(Authentication authentication) {
        String email = authentication.getName();
        Student student = studentService.getStudentByEmail(email);
        return ResponseEntity.ok(studentMapper.toResponseDTO(student));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PutMapping("/phone")
    public ResponseEntity<StudentResponseDTO> updatePhone(Principal principal, @Valid @RequestBody StudentPhoneUpdateRequestDTO updateRequest) {
        String email = principal.getName();
        Student student = studentService.updateStudentPhone(email, updateRequest.getPhone());
        return ResponseEntity.ok(studentMapper.toResponseDTO(student));
    }
}
