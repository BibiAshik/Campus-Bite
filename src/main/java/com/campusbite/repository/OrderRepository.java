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
}
