package org.example.order_app_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.request.CreateDisputeRequest;
import org.example.order_app_backend.dto.request.ResolveDisputeRequest;
import org.example.order_app_backend.dto.response.DisputeResponse;
import org.example.order_app_backend.entity.*;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.mapper.DisputeMapper;
import org.example.order_app_backend.repository.DisputeRepository;
import org.example.order_app_backend.repository.OrderRepository;
import org.example.order_app_backend.security.CurrentUserProvider;
import org.example.order_app_backend.service.DisputeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final OrderRepository orderRepository;
    private final DisputeMapper disputeMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public DisputeResponse createDispute(CreateDisputeRequest request) {
        log.info("Bắt đầu xử lý gửi khiếu nại cho đơn hàng ID: {}", request.getOrderId());

        // BR-DIS-02: Ràng buộc bằng chứng hình ảnh bắt buộc
        if (request.getEvidenceImages() == null || request.getEvidenceImages().isEmpty()) {
            log.warn("Khiếu nại bị từ chối do thiếu ảnh bằng chứng");
            throw new AppException(ErrorCode.DISPUTE_EVIDENCE_REQUIRED);
        }

        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateOrderAccess(order);

        // BR-DIS-01: Mỗi đơn hàng chỉ được tạo tối đa 01 khiếu nại
        if (disputeRepository.existsByOrderId(order.getId())) {
            log.warn("Đơn hàng ID {} đã có hồ sơ khiếu nại trước đó", order.getId());
            throw new AppException(ErrorCode.DISPUTE_ALREADY_EXISTS);
        }

        // BR-DIS-01: Chỉ khiếu nại đơn đã giao thành công DELIVERED
        if (order.getStatus() != OrderStatus.DELIVERED) {
            log.warn("Đơn hàng ID {} chưa giao nhận xong, trạng thái hiện tại: {}", order.getId(), order.getStatus());
            throw new AppException(ErrorCode.ORDER_NOT_DELIVERED);
        }

        // BR-DIS-01: Kiểm tra thời hạn khiếu nại 24 giờ kể từ lúc nhận hàng
        if (order.getDeliveredAt() != null) {
            Instant expiryTime = order.getDeliveredAt().plus(Duration.ofHours(24));
            if (Instant.now().isAfter(expiryTime)) {
                log.warn("Đơn hàng ID {} đã quá hạn khiếu nại 24 giờ", order.getId());
                throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION);
            }
        }

        // BR-DIS-03: Thẩm định số tiền hoàn: 0 < refundRequestedAmount <= finalAmount
        BigDecimal requestedAmount = request.getRefundRequestedAmount();
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0 || requestedAmount.compareTo(order.getFinalAmount()) > 0) {
            log.warn("Số tiền yêu cầu hoàn {} vượt quá tổng tiền đơn hàng {}", requestedAmount, order.getFinalAmount());
            throw new AppException(ErrorCode.REFUND_EXCEEDS_ORDER_TOTAL);
        }

        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = order.getUserId();
        }

        String serializedImages = disputeMapper.serializeEvidenceImages(request.getEvidenceImages());

        DisputeEntity dispute = DisputeEntity.builder()
                .orderId(order.getId())
                .userId(currentUserId)
                .reason(request.getReason())
                .description(request.getDescription())
                .evidenceImages(serializedImages)
                .refundRequestedAmount(requestedAmount)
                .status(DisputeStatus.PENDING)
                .build();

        DisputeEntity savedDispute = disputeRepository.save(dispute);
        log.info("Tạo hồ sơ khiếu nại thành công ID: {} cho đơn: {}", savedDispute.getId(), order.getOrderCode());

        return disputeMapper.toResponse(savedDispute, order.getOrderCode());
    }

    @Override
    public DisputeResponse getDisputeById(Long id) {
        DisputeEntity dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));

        OrderEntity order = orderRepository.findById(dispute.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateDisputeAccess(dispute);

        return disputeMapper.toResponse(dispute, order.getOrderCode());
    }

    @Override
    public List<DisputeResponse> getMyDisputes() {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = 1L;
        }

        List<DisputeEntity> disputes = disputeRepository.findByUserIdOrderByCreatedAtDesc(currentUserId);
        return disputes.stream().map(dispute -> {
            String orderCode = orderRepository.findById(dispute.getOrderId())
                    .map(OrderEntity::getOrderCode)
                    .orElse("KTX-ORDER");
            return disputeMapper.toResponse(dispute, orderCode);
        }).toList();
    }

    @Override
    @Transactional
    public DisputeResponse resolveDispute(Long id, ResolveDisputeRequest request) {
        log.info("Xử lý thẩm định khiếu nại ID: {} với trạng thái: {}", id, request.getStatus());

        DisputeEntity dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));

        OrderEntity order = orderRepository.findById(dispute.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        dispute.setStatus(request.getStatus());
        dispute.setResolutionNote(request.getResolutionNote());

        if (request.getStatus() == DisputeStatus.APPROVED) {
            BigDecimal approvedAmount = request.getRefundApprovedAmount() != null
                    ? request.getRefundApprovedAmount()
                    : dispute.getRefundRequestedAmount();

            if (approvedAmount.compareTo(order.getFinalAmount()) > 0) {
                throw new AppException(ErrorCode.REFUND_EXCEEDS_ORDER_TOTAL);
            }

            dispute.setRefundApprovedAmount(approvedAmount);

            // Nếu hoàn tiền toàn bộ hoặc phần lớn, cập nhật trạng thái thanh toán sang REFUNDED
            if (approvedAmount.compareTo(order.getFinalAmount()) >= 0) {
                order.setPaymentStatus(PaymentStatus.REFUNDED);
                orderRepository.save(order);
            }
        }

        DisputeEntity savedDispute = disputeRepository.save(dispute);
        log.info("Thẩm định khiếu nại ID: {} hoàn tất với trạng thái: {}", id, savedDispute.getStatus());

        return disputeMapper.toResponse(savedDispute, order.getOrderCode());
    }

    private void validateOrderAccess(OrderEntity order) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        boolean isAdmin = currentUserProvider.hasRole("ADMIN");

        if (currentUserId != null && !isAdmin && !order.getUserId().equals(currentUserId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateDisputeAccess(DisputeEntity dispute) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        boolean isAdmin = currentUserProvider.hasRole("ADMIN") || currentUserProvider.hasRole("STORE_OWNER");

        if (currentUserId != null && !isAdmin && !dispute.getUserId().equals(currentUserId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }
}

