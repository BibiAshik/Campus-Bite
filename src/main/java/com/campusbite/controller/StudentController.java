package com.campusbite.controller;

import com.campusbite.dto.request.StudentPhoneUpdateRequestDTO;
import com.campusbite.dto.response.StudentResponseDTO;
import com.campusbite.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/profile")
    public ResponseEntity<StudentResponseDTO> getProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(studentService.getStudentByEmail(email));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PutMapping("/phone")
    public ResponseEntity<StudentResponseDTO> updatePhone(@Valid @RequestBody StudentPhoneUpdateRequestDTO request, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(studentService.updateStudentPhone(email, request.getPhone()));
    }
}
