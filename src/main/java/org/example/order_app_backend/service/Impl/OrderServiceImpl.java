package org.example.order_app_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.common.PageResponse;
import org.example.order_app_backend.dto.request.CreateOrderRequest;
import org.example.order_app_backend.dto.request.OrderItemRequest;
import org.example.order_app_backend.dto.request.SelectedItemOptionDto;
import org.example.order_app_backend.dto.response.OrderResponse;
import org.example.order_app_backend.entity.*;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.mapper.OrderMapper;
import org.example.order_app_backend.repository.MenuItemRepository;
import org.example.order_app_backend.repository.NotificationRepository;
import org.example.order_app_backend.repository.OrderItemRepository;
import org.example.order_app_backend.repository.OrderRepository;
import org.example.order_app_backend.repository.StoreRepository;
import org.example.order_app_backend.repository.UserRepository;
import org.example.order_app_backend.security.CurrentUserProvider;
import org.example.order_app_backend.service.FcmNotificationService;
import org.example.order_app_backend.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal STANDARD_DELIVERY_FEE = BigDecimal.valueOf(15000.00);
    private static final BigDecimal KTX_GROUP_DELIVERY_FEE = BigDecimal.valueOf(5000.00);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreRepository storeRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final FcmNotificationService fcmNotificationService;
    private final OrderMapper orderMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Bắt đầu xử lý tạo đơn hàng cho storeId: {}", request.getStoreId());

        // BR-ORD-01: Ràng buộc Giỏ hàng không rỗng
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.ORDER_EMPTY);
        }

        // BR-ORD-01: Kiểm tra quán ăn tồn tại và đang hoạt động
        StoreEntity store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        if (Boolean.FALSE.equals(store.getIsActive())) {
            throw new AppException(ErrorCode.STORE_INACTIVE);
        }

        // Lấy danh sách ID các món ăn để kiểm tra tính hợp lệ
        List<Long> menuItemIds = request.getItems().stream()
                .map(OrderItemRequest::getMenuItemId)
                .toList();

        Map<Long, MenuItemEntity> menuItemsMap = menuItemRepository.findByIdIn(menuItemIds).stream()
                .collect(Collectors.toMap(MenuItemEntity::getId, Function.identity()));

        BigDecimal subtotalAmount = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        // BR-ORD-01 & BR-ORD-02: Duyệt từng món, kiểm tra khả dụng và thực hiện Price Snapshot
        for (OrderItemRequest itemReq : request.getItems()) {
            MenuItemEntity menuItem = menuItemsMap.get(itemReq.getMenuItemId());
            if (menuItem == null || !menuItem.getStoreId().equals(store.getId())) {
                throw new AppException(ErrorCode.MENU_ITEM_NOT_FOUND);
            }

            if (Boolean.FALSE.equals(menuItem.getIsAvailable())) {
                throw new AppException(ErrorCode.MENU_ITEM_UNAVAILABLE);
            }

            // Tính tổng tiền extra từ selected options
            BigDecimal extraPricePerUnit = BigDecimal.ZERO;
            if (itemReq.getSelectedOptions() != null) {
                for (SelectedItemOptionDto opt : itemReq.getSelectedOptions()) {
                    if (opt.getExtraPrice() != null) {
                        extraPricePerUnit = extraPricePerUnit.add(opt.getExtraPrice());
                    }
                }
            }

            // Price snapshot: Lưu giá gốc và tổng tiền món
            BigDecimal unitPriceWithExtra = menuItem.getBasePrice().add(extraPricePerUnit);
            BigDecimal itemTotalAmount = unitPriceWithExtra.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotalAmount = subtotalAmount.add(itemTotalAmount);

            String serializedOptions = orderMapper.serializeSelectedOptions(itemReq.getSelectedOptions());

            OrderItemEntity orderItemEntity = OrderItemEntity.builder()
                    .menuItemId(menuItem.getId())
                    .itemName(menuItem.getName())
                    .unitPrice(menuItem.getBasePrice())
                    .quantity(itemReq.getQuantity())
                    .selectedOptions(serializedOptions)
                    .itemTotalAmount(itemTotalAmount)
                    .build();

            orderItems.add(orderItemEntity);
        }

        // BR-ORD-03: Tính phí giao hàng gom đơn KTX
        BigDecimal deliveryFee = (request.getDeliveryFee() != null && request.getDeliveryFee().compareTo(BigDecimal.ZERO) >= 0)
                ? request.getDeliveryFee()
                : (isKtxSlotTime(request.getSlotTime()) ? KTX_GROUP_DELIVERY_FEE : STANDARD_DELIVERY_FEE);
        BigDecimal groupDiscountAmount = BigDecimal.ZERO;
        if (request.getGroupDiscountAmount() != null && request.getGroupDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            groupDiscountAmount = request.getGroupDiscountAmount();
        }
        BigDecimal finalAmount = subtotalAmount.add(deliveryFee).subtract(groupDiscountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        // Lấy User ID từ Security Context
        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = 1L; // Fallback mặc định cho dev / demo
        }

        // BR-USR-01: Kiểm tra tài khoản người dùng có bị khóa hay không
        UserEntity currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (Boolean.TRUE.equals(currentUser.getIsBanned())) {
            throw new AppException(ErrorCode.USER_BANNED);
        }

        // BR-ORD-05: Sinh mã đơn, mã QR đối soát và mã PIN 4 số
        String orderCode = generateOrderCode();
        String pickupQrCode = "QR_KTX_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        String pickupPin = String.format("%04d", ThreadLocalRandom.current().nextInt(1000, 10000));

        OrderEntity orderEntity = OrderEntity.builder()
                .orderCode(orderCode)
                .userId(currentUserId)
                .storeId(store.getId())
                .slotTime(request.getSlotTime())
                .pickupLocation(request.getPickupLocation())
                .subtotalAmount(subtotalAmount)
                .deliveryFee(deliveryFee)
                .groupDiscountAmount(groupDiscountAmount)
                .finalAmount(finalAmount)
                .status(OrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
                .pickupQrCode(pickupQrCode)
                .pickupPin(pickupPin)
                .note(request.getNote())
                .build();

        OrderEntity savedOrder = orderRepository.save(orderEntity);

        // Gán orderId cho toàn bộ order_items và lưu
        orderItems.forEach(item -> item.setOrderId(savedOrder.getId()));
        List<OrderItemEntity> savedItems = orderItemRepository.saveAll(orderItems);

        log.info("Tạo đơn hàng thành công với orderCode: {}, finalAmount: {}", savedOrder.getOrderCode(), savedOrder.getFinalAmount());
        return orderMapper.toResponse(savedOrder, savedItems, store.getName());
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateOrderAccess(order);

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(order.getId());
        String storeName = storeRepository.findById(order.getStoreId())
                .map(StoreEntity::getName)
                .orElse("Quán KTX");

        return orderMapper.toResponse(order, items, storeName);
    }

    @Override
    public List<OrderResponse> getMyOrderHistory() {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = 1L;
        }

        UserEntity user = userRepository.findById(currentUserId).orElse(null);
        List<OrderEntity> orders;
        if (user != null && user.getRole() == UserRole.ADMIN) {
            orders = orderRepository.findAll();
        } else {
            orders = orderRepository.findByUserIdOrderByCreatedAtDesc(currentUserId);
        }
        return orders.stream()
                .map(order -> {
                    List<OrderItemEntity> items = orderItemRepository.findByOrderId(order.getId());
                    String storeName = storeRepository.findById(order.getStoreId())
                            .map(StoreEntity::getName)
                            .orElse("Quán KTX");
                    return orderMapper.toResponse(order, items, storeName);
                })
                .toList();
    }

    @Override
    public PageResponse<OrderResponse> getMyOrders(Pageable pageable) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = 1L;
        }

        Page<OrderEntity> page = orderRepository.findByUserId(currentUserId, pageable);
        List<OrderResponse> content = page.getContent().stream()
                .map(order -> {
                    List<OrderItemEntity> items = orderItemRepository.findByOrderId(order.getId());
                    String storeName = storeRepository.findById(order.getStoreId())
                            .map(StoreEntity::getName)
                            .orElse("Quán KTX");
                    return orderMapper.toResponse(order, items, storeName);
                })
                .toList();

        return PageResponse.<OrderResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateOrderAccess(order);

        // BR-ORD-06: Chỉ được hủy đơn khi đang ở trạng thái PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Không thể hủy đơn hàng ID {} vì trạng thái hiện tại là: {}", id, order.getStatus());
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
        }

        order.setStatus(OrderStatus.CANCELLED);
        OrderEntity savedOrder = orderRepository.save(order);

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(savedOrder.getId());
        String storeName = storeRepository.findById(savedOrder.getStoreId())
                .map(StoreEntity::getName)
                .orElse("Quán KTX");

        log.info("Hủy đơn hàng thành công cho orderCode: {}", savedOrder.getOrderCode());
        return orderMapper.toResponse(savedOrder, items, storeName);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, org.example.order_app_backend.dto.request.UpdateOrderStatusRequest request) {
        log.info("Cập nhật trạng thái đơn hàng ID: {} sang {}", orderId, request.getStatus());

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus targetStatus = request.getStatus();

        // Idempotency: nếu đơn hàng đã ở đúng trạng thái yêu cầu thì trả về thành công ngay, tránh lỗi 422
        if (currentStatus == targetStatus) {
            log.info("Đơn hàng ID {} đã ở sẵn trạng thái {}, giữ nguyên.", orderId, targetStatus);
            List<OrderItemEntity> items = orderItemRepository.findByOrderId(order.getId());
            String storeName = storeRepository.findById(order.getStoreId())
                    .map(StoreEntity::getName)
                    .orElse("Quán KTX");
            return orderMapper.toResponse(order, items, storeName);
        }

        // Không cho phép chuyển từ trạng thái kết thúc (DELIVERED, CANCELLED)
        if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.CANCELLED) {
            log.warn("Đơn hàng ID {} đã ở trạng thái kết thúc {}, không thể chuyển sang {}", orderId, currentStatus, targetStatus);
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        order.setStatus(targetStatus);
        if (targetStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(Instant.now());
            if (order.getPaymentMethod() == PaymentMethod.COD) {
                order.setPaymentStatus(PaymentStatus.PAID);
            }
        }

        OrderEntity savedOrder = orderRepository.save(order);
        List<OrderItemEntity> items = orderItemRepository.findByOrderId(savedOrder.getId());
        String storeName = storeRepository.findById(savedOrder.getStoreId())
                .map(StoreEntity::getName)
                .orElse("Quán KTX");

        return orderMapper.toResponse(savedOrder, items, storeName);
    }

    @Override
    @Transactional
    public org.example.order_app_backend.dto.response.PickupConfirmationResponse confirmPickup(
            Long orderId,
            org.example.order_app_backend.dto.request.ConfirmPickupRequest request) {
        log.info("Xác nhận nhận hàng tại cổng KTX cho đơn hàng ID: {}, method: {}", orderId, request.getVerificationMethod());

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // BR-ORD-05: Đơn hàng phải ở trạng thái DELIVERING mới có thể xác nhận nhận hàng
        if (order.getStatus() != OrderStatus.DELIVERING) {
            log.warn("Đơn hàng ID {} ở trạng thái {} không thể xác nhận nhận hàng (yêu cầu DELIVERING)", orderId, order.getStatus());
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        // BR-ORD-05: Kiểm tra mã đối soát QR hoặc PIN
        String method = request.getVerificationMethod();
        String code = request.getCode() != null ? request.getCode().trim() : "";
        boolean isValid = false;

        if ("STUDENT_CONFIRM".equalsIgnoreCase(method) || "DIRECT".equalsIgnoreCase(method)) {
            isValid = true;
        } else if ("QR_SCAN".equalsIgnoreCase(method)) {
            isValid = code.equalsIgnoreCase(order.getPickupQrCode()) || code.equals(order.getPickupPin());
        } else if ("PIN".equalsIgnoreCase(method)) {
            isValid = code.equals(order.getPickupPin()) || code.equalsIgnoreCase(order.getPickupQrCode());
        } else {
            isValid = code.equalsIgnoreCase(order.getPickupQrCode()) || code.equals(order.getPickupPin()) || !code.isEmpty();
        }

        if (!isValid) {
            log.warn("Mã đối soát không hợp lệ cho đơn hàng ID: {}, input: {}", orderId, code);
            throw new AppException(ErrorCode.INVALID_PICKUP_CODE);
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(Instant.now());
        if (order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        OrderEntity savedOrder = orderRepository.save(order);
        log.info("Xác nhận giao nhận thành công đơn hàng {}", savedOrder.getOrderCode());

        return org.example.order_app_backend.dto.response.PickupConfirmationResponse.builder()
                .orderId(savedOrder.getId())
                .status(savedOrder.getStatus())
                .deliveredAt(savedOrder.getDeliveredAt())
                .build();
    }

    @Override
    @Transactional
    public OrderResponse notifyArrival(Long orderId) {
        log.info("Shipper thông báo đã đến điểm nhận hàng cho đơn ID: {}", orderId);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        order.setShipperArrived(true);
        if (order.getStatus() == OrderStatus.PREPARING || order.getStatus() == OrderStatus.READY) {
            order.setStatus(OrderStatus.DELIVERING);
        }
        OrderEntity savedOrder = orderRepository.save(order);

        String storeName = storeRepository.findById(savedOrder.getStoreId())
                .map(StoreEntity::getName)
                .orElse("Quán KTX");

        try {
            if (notificationRepository != null) {
                notificationRepository.save(NotificationEntity.builder()
                        .userId(savedOrder.getUserId())
                        .title("🚚 Shipper đã tới Cổng KTX!")
                        .body("Đơn hàng #" + savedOrder.getOrderCode() + " từ " + storeName + " đã có mặt tại " + savedOrder.getPickupLocation() + ". Vui lòng ra nhận món!")
                        .type(NotificationType.ORDER_STATUS)
                        .dataPayload("SHIPPER_ARRIVED:" + savedOrder.getId())
                        .isRead(false)
                        .build());
            }
        } catch (Exception e) {
            log.warn("Không thể lưu notification cho đơn hàng {}: {}", orderId, e.getMessage());
        }

        try {
            if (fcmNotificationService != null) {
                fcmNotificationService.sendPushNotification(
                        savedOrder.getUserId(),
                        "🚚 Shipper đã tới Cổng KTX!",
                        "Đơn hàng #" + savedOrder.getOrderCode() + " đã tới điểm hẹn. Mang mã QR / PIN ra nhận món nhé!",
                        Map.of("orderId", String.valueOf(savedOrder.getId()), "event", "SHIPPER_ARRIVED")
                );
            }
        } catch (Exception e) {
            log.warn("Lỗi gửi FCM cho đơn hàng {}: {}", orderId, e.getMessage());
        }

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(savedOrder.getId());
        return orderMapper.toResponse(savedOrder, items, storeName);
    }

    private void validateOrderAccess(OrderEntity order) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        boolean isAdmin = currentUserProvider.hasRole("ADMIN");

        if (currentUserId != null && !isAdmin && !order.getUserId().equals(currentUserId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

    private boolean isKtxSlotTime(String slotTime) {
        if (slotTime == null) {
            return false;
        }
        String upper = slotTime.toUpperCase();
        return upper.contains("CA_") || upper.contains("KTX") || upper.contains("SLOT");
    }

    private String generateOrderCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        return "KTX-" + datePart + "-" + randomSuffix;
    }
}
