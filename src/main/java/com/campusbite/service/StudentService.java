package com.campusbite.service;

import com.campusbite.entity.Student;
import com.campusbite.exception.ResourceNotFoundException;
import com.campusbite.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
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
    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    /**
     * Purpose: Student updates their phone number from Profile page.
     */
    public Student updateStudentPhone(String email, String phone) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        student.setPhone(phone);
        return studentRepository.save(student);
    }
}
