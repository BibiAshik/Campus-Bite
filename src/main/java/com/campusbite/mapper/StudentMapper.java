package com.campusbite.mapper;

import com.campusbite.dto.response.StudentResponseDTO;
import com.campusbite.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public StudentResponseDTO toResponseDTO(Student student) {
        StudentResponseDTO dto = new StudentResponseDTO();
        dto.setId(student.getId());
        dto.setEmail(student.getEmail());
        dto.setName(student.getName());
        dto.setProfileImageUrl(student.getProfileImageUrl());
        dto.setPhone(student.getPhone());
        dto.setCreatedAt(student.getCreatedAt());
        return dto;
    }
}
