package com.campusbite.mapper;

import com.campusbite.dto.response.PaymentResponseDTO;
import com.campusbite.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponseDTO toResponseDTO(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setOrderToken(payment.getOrder().getTokenNumber());
        dto.setStudentEmail(payment.getStudentEmail());
        dto.setRazorpayOrderId(payment.getRazorpayOrderId());
        dto.setRazorpayPaymentId(payment.getRazorpayPaymentId());
        dto.setStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
        dto.setAmount(payment.getAmount());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}
