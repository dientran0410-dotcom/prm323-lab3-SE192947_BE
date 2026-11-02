package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.request.CreateOrderRequest;
import org.example.order_app_backend.dto.response.OrderResponse;
import org.example.order_app_backend.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "Các API tạo đơn hàng, tính tiền snapshot, xem chi tiết và lịch sử đơn hàng KTX")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo đơn hàng gom đơn KTX (API-006)", description = "Xác nhận đặt giỏ hàng, áp dụng ca gom đơn KTX để chia ship, sinh snapshot giá và mã đối soát QR/PIN")
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Nhận yêu cầu tạo đơn hàng gom đơn KTX cho quán ID: {}", request.getStoreId());
        OrderResponse response = orderService.createOrder(request);
        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Tạo đơn hàng thành công")
                .data(response)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết đơn hàng & theo dõi (API-007)", description = "Lấy đầy đủ thông tin trạng thái đơn hàng, danh sách món với price snapshot, và mã QR đối soát")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Lấy chi tiết đơn hàng thành công")
                .data(response)
                .build();
    }

    @GetMapping
    @Operation(summary = "Lịch sử đơn hàng của người dùng (API-008)", description = "Danh sách tất cả các đơn hàng đã đặt của sinh viên hiện tại")
    public ApiResponse<List<OrderResponse>> getOrderHistory() {
        List<OrderResponse> orders = orderService.getMyOrderHistory();
        return ApiResponse.<List<OrderResponse>>builder()
                .success(true)
                .message("Lấy lịch sử đơn hàng thành công")
                .data(orders)
                .build();
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Hủy đơn hàng (API-009)", description = "Cho phép sinh viên hủy đơn hàng khi đơn còn ở trạng thái PENDING")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable Long id) {
        log.info("Nhận yêu cầu hủy đơn hàng ID: {}", id);
        OrderResponse response = orderService.cancelOrder(id);
        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Hủy đơn hàng thành công")
                .data(response)
                .build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái đơn hàng (API-010)", description = "Quán ăn / Shipper cập nhật trạng thái đơn: PREPARING, READY, DELIVERING")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody org.example.order_app_backend.dto.request.UpdateOrderStatusRequest request) {
        log.info("Nhận yêu cầu cập nhật trạng thái đơn hàng ID: {} sang {}", id, request.getStatus());
        OrderResponse response = orderService.updateOrderStatus(id, request);
        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Cập nhật trạng thái đơn hàng thành công")
                .data(response)
                .build();
    }

    @PostMapping("/{id}/confirm-pickup")
    @Operation(summary = "Xác nhận nhận hàng tại trụ KTX (API-011)", description = "Đối soát bằng QR scan hoặc PIN 4 số cuối SĐT, chuyển trạng thái đơn sang DELIVERED")
    public ApiResponse<org.example.order_app_backend.dto.response.PickupConfirmationResponse> confirmPickup(
            @PathVariable Long id,
            @Valid @RequestBody org.example.order_app_backend.dto.request.ConfirmPickupRequest request) {
        log.info("Nhận yêu cầu xác nhận nhận hàng ID: {}, method: {}", id, request.getVerificationMethod());
        org.example.order_app_backend.dto.response.PickupConfirmationResponse response = orderService.confirmPickup(id, request);
        return ApiResponse.<org.example.order_app_backend.dto.response.PickupConfirmationResponse>builder()
                .success(true)
                .message("Xác nhận giao nhận thành công")
                .data(response)
                .build();
    }
}

