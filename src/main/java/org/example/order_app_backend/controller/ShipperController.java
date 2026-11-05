package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.request.WithdrawalRequest;
import org.example.order_app_backend.dto.response.OrderResponse;
import org.example.order_app_backend.dto.response.ShipperBatchDeliveryResponse;
import org.example.order_app_backend.dto.response.ShipperWalletResponse;
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

    @PatchMapping("/orders/{id}/cancel-pickup")
    @Operation(summary = "Shipper hủy nhận đơn / trả lại quán", description = "Hủy nhận đơn do xe hỏng hoặc sự cố đột xuất, chuyển đơn về lại READY cho shipper khác")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelPickup(
            @PathVariable("id") Long id,
            @RequestParam(value = "reason", required = false) String reason) {
        OrderResponse response = shipperService.cancelPickupOrder(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Đã hủy nhận đơn và trả lại bếp KTX thành công", response));
    }

    @PostMapping("/orders/{id}/report-failure")
    @Operation(summary = "Báo cáo giao không thành công / Khách bom hàng", description = "Shipper ghi nhận khách không nhận hoặc không liên lạc được để đối soát")
    public ResponseEntity<ApiResponse<OrderResponse>> reportFailure(
            @PathVariable("id") Long id,
            @RequestParam(value = "reason", required = false) String reason) {
        OrderResponse response = shipperService.reportDeliveryFailure(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Đã ghi nhận báo cáo giao không thành công / Khách bom hàng", response));
    }

    @PatchMapping("/orders/{id}/deliver")
    @Operation(summary = "Xác nhận trao hàng tại cổng KTX", description = "Shipper giao hàng thành công tại cổng KTX và hoàn tất đơn")
    public ResponseEntity<ApiResponse<OrderResponse>> deliverOrder(@PathVariable("id") Long id) {
        OrderResponse response = shipperService.deliverOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Đã giao hàng thành công tại cổng KTX", response));
    }

    @GetMapping("/wallet")
    @Operation(summary = "Xem thông tin ví thu nhập và đối soát", description = "Lấy số dư ví, tiền thù lao chờ đối soát và lịch sử giao dịch ví")
    public ResponseEntity<ApiResponse<ShipperWalletResponse>> getWallet() {
        ShipperWalletResponse response = shipperService.getWallet();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin ví thu nhập thành công", response));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Yêu cầu rút tiền thù lao", description = "Gửi yêu cầu rút thù lao tích lũy về tài khoản ngân hàng")
    public ResponseEntity<ApiResponse<Void>> withdraw(@Valid @RequestBody WithdrawalRequest request) {
        shipperService.withdraw(request);
        return ResponseEntity.ok(ApiResponse.success("Yêu cầu rút tiền thù lao đã được tiếp nhận", null));
    }
}