package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.response.AdminDashboardResponse;
import org.example.order_app_backend.dto.response.DisputeResponse;
import org.example.order_app_backend.dto.response.OrderResponse;
import org.example.order_app_backend.dto.response.UserResponse;
import org.example.order_app_backend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Controller", description = "Các API dành cho Ban Quản trị KTX")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/orders")
    @Operation(summary = "Lấy tất cả đơn hàng gom ca trên hệ thống", description = "Quản trị viên giám sát toàn bộ đơn hàng gom ca ở tất cả các tòa KTX")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> response = adminService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tất cả đơn hàng thành công", response));
    }

    @GetMapping("/dashboard-stats")
    @Operation(summary = "Lấy dữ liệu thống kê tổng quan Web Admin", description = "Trả về doanh thu, số đơn, khiếu nại và người dùng phục vụ Cổng Web Admin KTX")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboardStats() {
        AdminDashboardResponse response = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Lấy số liệu thống kê quản trị thành công", response));
    }

    @GetMapping("/disputes")
    @Operation(summary = "Lấy tất cả khiếu nại trên hệ thống", description = "Quản trị viên xem toàn bộ danh sách khiếu nại của sinh viên")
    public ResponseEntity<ApiResponse<List<DisputeResponse>>> getAllDisputes() {
        List<DisputeResponse> response = adminService.getAllDisputes();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tất cả khiếu nại thành công", response));
    }

    @GetMapping("/users")
    @Operation(summary = "Lấy danh sách người dùng", description = "Quản trị viên xem danh sách tài khoản sinh viên")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> response = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công", response));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Cập nhật quyền vai trò người dùng", description = "Đổi role giữa STUDENT, STORE_OWNER, SHIPPER, ADMIN")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable("id") Long id,
            @RequestParam("role") String role) {
        UserResponse response = adminService.updateUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật vai trò người dùng thành công", response));
    }

    @PatchMapping({"/users/{id}/toggle-ban", "/users/{id}/ban"})
    @Operation(summary = "Khóa hoặc mở khóa tài khoản người dùng (Ban/Lock account)", description = "Ban Quản lý KTX xử lý vi phạm, đình chỉ hoặc mở lại tài khoản")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserBan(
            @PathVariable("id") Long id,
            @RequestParam(value = "banned", required = false) Boolean banned) {
        UserResponse response = adminService.toggleUserBan(id, banned);
        String actionMsg = Boolean.TRUE.equals(response.getIsBanned()) ? "Đã khóa tài khoản người dùng thành công" : "Đã mở khóa tài khoản người dùng thành công";
        return ResponseEntity.ok(ApiResponse.success(actionMsg, response));
    }
}
