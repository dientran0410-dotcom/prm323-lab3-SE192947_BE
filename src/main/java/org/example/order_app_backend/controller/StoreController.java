package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.common.PageResponse;
import org.example.order_app_backend.dto.request.CreateStoreRequest;
import org.example.order_app_backend.dto.request.UpdateStoreRequest;
import org.example.order_app_backend.dto.response.StoreResponse;
import org.example.order_app_backend.service.StoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Tag(name = "03. Store Management", description = "Các API khám phá, tìm kiếm và quản lý thông tin quán ăn KTX")
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    @Operation(summary = "Lấy danh sách & Tìm kiếm quán ăn (Flow 1 - S1 Home)",
            description = "Hỗ trợ lọc theo phân khu KTX (KTX_KHU_A, KTX_KHU_B), tìm kiếm từ khóa món ăn/tên quán và phân trang.")
    public ResponseEntity<ApiResponse<PageResponse<StoreResponse>>> getStores(
            @Parameter(description = "Từ khóa tìm kiếm (tên quán, món ăn)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Phân khu KTX: KTX_KHU_A hoặc KTX_KHU_B") @RequestParam(required = false) String campusZone,
            @Parameter(description = "Trạng thái mở cửa") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<StoreResponse> stores = storeService.getStores(keyword, campusZone, isActive, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách quán ăn thành công", stores));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết quán ăn (Flow 1 - S2 Store Detail)",
            description = "Truy xuất thông tin chi tiết một quán ăn theo mã định danh ID.")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(@PathVariable Long id) {
        StoreResponse store = storeService.getStoreById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin quán ăn thành công", store));
    }

    @PostMapping
    @Operation(summary = "Tạo mới quán ăn (Dành cho Quản trị viên / Chủ quán)",
            description = "Thêm một quán ăn liên kết KTX mới vào hệ thống.")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@Valid @RequestBody CreateStoreRequest request) {
        StoreResponse store = storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo quán ăn mới thành công", store));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin quán ăn",
            description = "Chỉnh sửa tên quán, số điện thoại, trạng thái đóng/mở cửa.")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoreRequest request
    ) {
        StoreResponse store = storeService.updateStore(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin quán ăn thành công", store));
    }
}

