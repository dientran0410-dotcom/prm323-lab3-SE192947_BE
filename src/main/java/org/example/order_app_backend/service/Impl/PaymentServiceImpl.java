package org.example.order_app_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.response.PaymentResponse;
import org.example.order_app_backend.entity.*;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.mapper.PaymentMapper;
import org.example.order_app_backend.repository.OrderRepository;
import org.example.order_app_backend.repository.PaymentRepository;
import org.example.order_app_backend.security.CurrentUserProvider;
import org.example.order_app_backend.service.PaymentService;
import org.example.order_app_backend.service.gateway.PaymentQrResult;
import org.example.order_app_backend.service.gateway.VietQrPaymentGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final VietQrPaymentGateway vietQrPaymentGateway;
    private final PaymentMapper paymentMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public PaymentResponse generatePaymentQr(Long orderId) {
        log.info("Xử lý sinh mã VietQR cho đơn hàng ID: {}", orderId);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateOrderAccess(order);

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.warn("Đơn hàng ID {} đã hoàn tất thanh toán trước đó", orderId);
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION);
        }

        // BR-PAY-01: Sinh mã VietQR chuẩn hóa
        PaymentQrResult qrResult = vietQrPaymentGateway.generatePaymentQr(
                order.getId(),
                order.getOrderCode(),
                order.getFinalAmount()
        );

        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseGet(() -> PaymentEntity.builder()
                        .orderId(orderId)
                        .amount(order.getFinalAmount())
                        .status(PaymentStatus.UNPAID)
                        .build());

        payment.setPaymentMethod(PaymentMethod.VIETQR);
        payment.setAmount(order.getFinalAmount());
        payment.setVietqrContent(qrResult.getVietqrContent());
        payment.setQrUrl(qrResult.getQrUrl());

        PaymentEntity savedPayment = paymentRepository.save(payment);

        order.setPaymentMethod(PaymentMethod.VIETQR);
        orderRepository.save(order);

        log.info("Đã sinh thành công mã VietQR cho đơn hàng: {}", order.getOrderCode());
        return paymentMapper.toResponse(savedPayment, order.getOrderCode(), qrResult);
    }

    @Override
    @Transactional
    public PaymentResponse switchToCod(Long orderId) {
        log.info("Kích hoạt cơ chế dự phòng chuyển đổi sang COD cho đơn hàng ID: {}", orderId);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateOrderAccess(order);

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.warn("Không thể chuyển sang COD vì đơn hàng {} đã thanh toán", order.getOrderCode());
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION);
        }

        // BR-PAY-02: Chuyển đổi phương thức sang COD, trạng thái UNPAID và đơn sang CONFIRMED
        order.setPaymentMethod(PaymentMethod.COD);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        }
        orderRepository.save(order);

        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseGet(() -> PaymentEntity.builder()
                        .orderId(orderId)
                        .amount(order.getFinalAmount())
                        .build());

        payment.setPaymentMethod(PaymentMethod.COD);
        payment.setStatus(PaymentStatus.UNPAID);
        payment.setQrUrl(null);
        payment.setVietqrContent(null);

        PaymentEntity savedPayment = paymentRepository.save(payment);

        log.info("Chuyển sang COD thành công cho đơn hàng: {}", order.getOrderCode());
        return paymentMapper.toResponse(savedPayment, order.getOrderCode(), null);
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateOrderAccess(order);

        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        PaymentQrResult qrResult = null;
        if (payment.getPaymentMethod() == PaymentMethod.VIETQR && payment.getQrUrl() != null) {
            qrResult = vietQrPaymentGateway.generatePaymentQr(order.getId(), order.getOrderCode(), payment.getAmount());
        }

        return paymentMapper.toResponse(payment, order.getOrderCode(), qrResult);
    }

    @Override
    @Transactional
    public PaymentResponse confirmPaymentSuccess(Long orderId, String transactionRef) {
        log.info("Xác nhận thanh toán thành công cho đơn hàng ID: {}, Ref: {}", orderId, transactionRef);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseGet(() -> PaymentEntity.builder()
                        .orderId(orderId)
                        .paymentMethod(order.getPaymentMethod())
                        .amount(order.getFinalAmount())
                        .build());

        String ref = (transactionRef != null && !transactionRef.isBlank())
                ? transactionRef
                : "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(Instant.now());
        payment.setTransactionRef(ref);
        PaymentEntity savedPayment = paymentRepository.save(payment);

        order.setPaymentStatus(PaymentStatus.PAID);
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        }
        orderRepository.save(order);

        log.info("Thanh toán thành công đơn hàng {} với mã giao dịch: {}", order.getOrderCode(), ref);
        return paymentMapper.toResponse(savedPayment, order.getOrderCode(), null);
    }

    private void validateOrderAccess(OrderEntity order) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        boolean isAdmin = currentUserProvider.hasRole("ADMIN");

        if (currentUserId != null && !isAdmin && !order.getUserId().equals(currentUserId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }
}

