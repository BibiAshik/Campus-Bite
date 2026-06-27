package com.campusbite.repository;

import com.campusbite.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByTokenNumber(String tokenNumber);
    List<Order> findByStatusOrderByOrderDateDesc(String status);
    List<Order> findByOrderDateAfter(LocalDateTime date);
    List<Order> findAllByOrderByOrderDateDesc();
    List<Order> findByStudentEmailOrderByOrderDateDesc(String studentEmail);
    List<Order> findByOrderDateBetweenOrderByOrderDateDesc(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByStatusAndOrderDateBetweenOrderByOrderDateDesc(String status, LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByStudentEmailAndOrderDateBetweenOrderByOrderDateDesc(String studentEmail, LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByStudentEmailAndStatusAndOrderDateBetweenOrderByOrderDateDesc(String studentEmail, String status, LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByStudentEmailAndStatusOrderByOrderDateDesc(String studentEmail, String status);
}
