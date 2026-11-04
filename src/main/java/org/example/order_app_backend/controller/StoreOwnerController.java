package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.dto.request.UpdateMenuItemAvailabilityRequest;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.response.MenuItemResponse;
import org.example.order_app_backend.dto.response.OwnerOrderSummaryResponse;
import org.example.order_app_backend.dto.response.StoreResponse;
import org.example.order_app_backend.service.StoreOwnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owner")
@RequiredArgsConstructor
@Tag(name = "Store Owner Controller", description = "Các API dành cho Chủ quán ăn KTX")
public class StoreOwnerController {

    private final StoreOwnerService storeOwnerService;

    @GetMapping("/store")
    @Operation(summary = "Lấy thông tin quán ăn của tôi", description = "Trả về thông tin chi tiết quán ăn của chủ quán")
    public ResponseEntity<ApiResponse<StoreResponse>> getMyStore() {
        StoreResponse response = storeOwnerService.getMyStore();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin quán thành công", response));
    }

    @GetMapping("/orders")
    @Operation(summary = "Xem danh sách đơn gom của quán theo ca", description = "Lấy danh sách các đơn hàng sinh viên gom theo ca trưa/chiều")
    public ResponseEntity<ApiResponse<List<OwnerOrderSummaryResponse>>> getMyStoreOrders(
            @RequestParam(value = "slotTime", required = false, defaultValue = "CA_TRUA_11H30") String slotTime) {
        List<OwnerOrderSummaryResponse> response = storeOwnerService.getMyStoreOrders(slotTime);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn gom thành công", response));
    }

    @PatchMapping("/menu-items/{id}/availability")
    @Operation(summary = "Bật/Tắt trạng thái còn món", description = "Cập nhật trạng thái còn món hay hết món cho quán")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItemAvailability(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateMenuItemAvailabilityRequest request) {
        MenuItemResponse response = storeOwnerService.updateMenuItemAvailability(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái món thành công", response));
    }
}