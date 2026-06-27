package com.campusbite.service;

import com.campusbite.dto.response.StudentResponseDTO;
import com.campusbite.entity.Student;
import com.campusbite.exception.ResourceNotFoundException;
import com.campusbite.mapper.StudentMapper;
import com.campusbite.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    public StudentService(StudentRepository studentRepository, StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }

    /**
     * Purpose: Called by OAuth2SuccessHandler after Google login. 
     * Finds existing Student by email or creates a new one if first login.
     */
    public Student findOrCreateStudent(String email, String name, String profileImageUrl) {
        return studentRepository.findByEmail(email).orElseGet(() -> {
            Student student = new Student();
            student.setEmail(email);
            student.setName(name);
            student.setProfileImageUrl(profileImageUrl);
            student.setCreatedAt(LocalDateTime.now());
            return studentRepository.save(student);
        });
    }

    /**
     * Purpose: Fetch student profile by email.
     */
    public StudentResponseDTO getStudentByEmail(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return studentMapper.toResponseDTO(student);
    }

    /**
     * Purpose: Student updates their phone number from Profile page.
     */
    public StudentResponseDTO updateStudentPhone(String email, String phone) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        student.setPhone(phone);
        Student updated = studentRepository.save(student);
        return studentMapper.toResponseDTO(updated);
    }
}
