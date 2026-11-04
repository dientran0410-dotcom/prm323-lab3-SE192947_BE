package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.response.OrderResponse;
import org.example.order_app_backend.dto.response.ShipperBatchDeliveryResponse;
import org.example.order_app_backend.service.ShipperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shipper")
@RequiredArgsConstructor
@Tag(name = "Shipper Controller", description = "Các API dành cho Tài xế / Đội Gom đơn KTX")
public class ShipperController {

    private final ShipperService shipperService;

    @GetMapping("/batches")
    @Operation(summary = "Xem danh sách chuyến gom theo ca", description = "Lấy toàn bộ đơn hàng trong ca gom trưa 11:30 hoặc chiều 18:00")
    public ResponseEntity<ApiResponse<ShipperBatchDeliveryResponse>> getBatchesBySlot(
            @RequestParam(value = "slotTime", required = false, defaultValue = "CA_TRUA_11H30") String slotTime) {
        ShipperBatchDeliveryResponse response = shipperService.getBatchesBySlot(slotTime);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chuyến gom thành công", response));
    }

    @PatchMapping("/orders/{id}/pickup")
    @Operation(summary = "Xác nhận nhận đơn tại quán", description = "Shipper nhận đơn từ quán để chuyển trạng thái sang DELIVERING")
    public ResponseEntity<ApiResponse<OrderResponse>> pickupOrder(@PathVariable("id") Long id) {
        OrderResponse response = shipperService.pickupOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Đã nhận đơn tại quán, bắt đầu giao đến cổng KTX", response));
    }
}