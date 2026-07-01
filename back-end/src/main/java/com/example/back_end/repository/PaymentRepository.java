package com.example.back_end.repository;

import com.example.back_end.entity.Payment;
import com.example.back_end.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudentId(Long studentId);
    List<Payment> findByTeacherId(Long teacherId);
    List<Payment> findByStudentIdAndStatus(Long studentId, PaymentStatus status);
}
