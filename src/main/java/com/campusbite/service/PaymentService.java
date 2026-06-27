package com.campusbite.service;

import com.campusbite.dto.response.PaymentResponseDTO;
import com.campusbite.entity.Order;
import com.campusbite.entity.Payment;
import com.campusbite.entity.PaymentStatus;
import com.campusbite.exception.PaymentVerificationException;
import com.campusbite.exception.ResourceNotFoundException;
import com.campusbite.mapper.PaymentMapper;
import com.campusbite.repository.OrderRepository;
import com.campusbite.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing payments via Razorpay.
 * Methods are ordered from simplest (read) to most complex (Razorpay gateway integration).
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentMapper = paymentMapper;
    }

    // --- Simple Read Operations ---

    public List<PaymentResponseDTO> getPaymentsByStudentEmail(String email, String searchId, LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByStudentEmailOrderByCreatedAtDesc(email).stream()
                .filter(p -> {
                    boolean matchesSearch = searchId == null || searchId.trim().isEmpty() || String.valueOf(p.getId()).contains(searchId.trim());
                    boolean matchesStartDate = startDate == null || !p.getCreatedAt().isBefore(startDate);
                    boolean matchesEndDate = endDate == null || !p.getCreatedAt().isAfter(endDate);
                    return matchesSearch && matchesStartDate && matchesEndDate;
                })
                .map(paymentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PaymentResponseDTO> getAllPayments(LocalDateTime startDate, LocalDateTime endDate) {
        List<Payment> payments;
        if (startDate != null && endDate != null) {
            payments = paymentRepository.findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(PaymentStatus.PAID, startDate, endDate);
        } else {
            // Default to PAID payments only, ordered by date
            payments = paymentRepository.findAll().stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PAID)
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .collect(Collectors.toList());
        }
        return payments.stream().map(paymentMapper::toResponseDTO).collect(Collectors.toList());
    }

    // --- Complex Transactional Operations (Razorpay Gateway) ---

    /**
     * Purpose: Creates a Razorpay Order for a specific order.
     * Communicates with the Razorpay API to initialize a payment session.
     */
    @Transactional
    public PaymentResponseDTO createRazorpayOrder(Long orderId, String studentEmail) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            if (!order.getStudentEmail().equals(studentEmail)) {
                throw new SecurityException("Not authorized to pay for this order");
            }

            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject options = new JSONObject();
            // Razorpay expects amount in paise
            options.put("amount", (int) (order.getTotalAmount() * 100));
            options.put("currency", "INR");
            options.put("receipt", "txn_" + order.getId());

            com.razorpay.Order razorpayOrder = client.orders.create(options);
            String rzpOrderId = razorpayOrder.get("id");

            order.setRazorpayOrderId(rzpOrderId);
            orderRepository.save(order);

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setStudentEmail(studentEmail);
            payment.setRazorpayOrderId(rzpOrderId);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setAmount(order.getTotalAmount());
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());

            Payment saved = paymentRepository.save(payment);
            return paymentMapper.toResponseDTO(saved);

        } catch (RazorpayException e) {
            throw new RuntimeException("Error creating Razorpay order: " + e.getMessage());
        }
    }

    /**
     * Purpose: Verifies Razorpay payment signature server-side and marks the order as PAID.
     * This is the most complex method — it calls the Razorpay Utils to cryptographically
     * verify the payment signature, then updates both the Order and Payment records atomically.
     */
    @Transactional
    public PaymentResponseDTO verifyAndCompletePayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature, String studentEmail) {
        Order order = orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with razorpayOrderId"));

        if (!order.getStudentEmail().equals(studentEmail)) {
            throw new SecurityException("Not authorized");
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (!isValid) {
                throw new PaymentVerificationException("Payment signature verification failed");
            }

            order.setPaymentStatus(PaymentStatus.PAID);
            order.setRazorpayPaymentId(razorpayPaymentId);
            orderRepository.save(order);

            Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));

            payment.setRazorpayPaymentId(razorpayPaymentId);
            payment.setStatus(PaymentStatus.PAID);
            payment.setUpdatedAt(LocalDateTime.now());
            Payment saved = paymentRepository.save(payment);

            return paymentMapper.toResponseDTO(saved);
        } catch (RazorpayException e) {
            throw new PaymentVerificationException("Error verifying payment: " + e.getMessage());
        }
    }
}

