package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.request.CreateMenuItemRequest;
import org.example.order_app_backend.dto.request.UpdateMenuItemRequest;
import org.example.order_app_backend.dto.response.MenuItemResponse;
import org.example.order_app_backend.service.MenuItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "04. Menu Management", description = "Các API tra cứu thực đơn, chi tiết món ăn và tùy biến Size/Topping")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping("/stores/{storeId}/menu-items")
    @Operation(summary = "Lấy danh sách thực đơn của quán (Flow 1 - S2 Store Detail, S3 Customization)",
            description = "Trả về danh sách món ăn của quán theo danh mục và tình trạng còn/hết hàng.")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenuItemsByStore(
            @PathVariable Long storeId,
            @Parameter(description = "Lọc theo danh mục: COM, BUN_PHO, NUOC, AN_VAT") @RequestParam(required = false) String category,
            @Parameter(description = "Chỉ lấy món còn hàng (true/false)") @RequestParam(required = false) Boolean availableOnly
    ) {
        List<MenuItemResponse> items = menuItemService.getMenuItemsByStore(storeId, category, availableOnly);
        return ResponseEntity.ok(ApiResponse.success("Lấy thực đơn quán ăn thành công", items));
    }

    @GetMapping("/menu-items/{id}")
    @Operation(summary = "Chi tiết món ăn & tùy chọn Size/Topping (Flow 1 - S3 Customization)",
            description = "Lấy đầy đủ thông tin món ăn, đơn giá gốc và danh sách nhóm tùy biến.")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getMenuItemById(@PathVariable Long id) {
        MenuItemResponse item = menuItemService.getMenuItemById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin món ăn thành công", item));
    }

    @PostMapping("/stores/{storeId}/menu-items")
    @Operation(summary = "Thêm món ăn mới vào thực đơn quán",
            description = "Tạo món ăn mới gắn liền với quán ăn chỉ định.")
    public ResponseEntity<ApiResponse<MenuItemResponse>> createMenuItem(
            @PathVariable Long storeId,
            @Valid @RequestBody CreateMenuItemRequest request
    ) {
        MenuItemResponse item = menuItemService.createMenuItem(storeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thêm món ăn mới thành công", item));
    }

    @PutMapping("/menu-items/{id}")
    @Operation(summary = "Cập nhật thông tin món ăn",
            description = "Cập nhật giá, mô tả, tình trạng còn hàng hoặc danh sách tùy biến.")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMenuItemRequest request
    ) {
        MenuItemResponse item = menuItemService.updateMenuItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin món ăn thành công", item));
    }
}

