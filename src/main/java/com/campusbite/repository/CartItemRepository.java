package com.campusbite.repository;

import com.campusbite.entity.CartItem;
import com.campusbite.entity.Student;
import com.campusbite.entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByStudent(Student student);

    Optional<CartItem> findByStudentAndFoodItem(Student student, FoodItem foodItem);

    void deleteByStudent(Student student);
}
