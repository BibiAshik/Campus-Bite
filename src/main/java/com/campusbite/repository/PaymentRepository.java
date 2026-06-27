package com.campusbite.repository;

import com.campusbite.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudentEmailOrderByCreatedAtDesc(String studentEmail);
    List<Payment> findByCreatedAtBetweenOrderByCreatedAtDesc(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    List<Payment> findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(com.campusbite.entity.PaymentStatus status, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
