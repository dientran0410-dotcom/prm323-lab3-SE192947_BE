package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.request.ConfirmPaymentRequest;
import org.example.order_app_backend.dto.response.PaymentResponse;
import org.example.order_app_backend.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "Các API thanh toán VietQR và cơ chế dự phòng chuyển đổi sang COD")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}/generate-qr")
    @Operation(summary = "Sinh link và mã VietQR thanh toán (API-012)", description = "Khởi tạo mã VietQR chuẩn Napas 247 kèm nội dung chuyển khoản ORD_<ORDER_ID>")
    public ApiResponse<PaymentResponse> generatePaymentQr(@PathVariable Long orderId) {
        log.info("Nhận yêu cầu sinh mã VietQR cho đơn hàng ID: {}", orderId);
        PaymentResponse response = paymentService.generatePaymentQr(orderId);
        return ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Sinh mã VietQR thanh toán thành công")
                .data(response)
                .build();
    }

    @PostMapping("/{orderId}/switch-to-cod")
    @Operation(summary = "Đổi phương thức thanh toán sang COD (API-013)", description = "Cơ chế dự phòng khi app ngân hàng lỗi hoặc hủy quét QR, chuyển đơn sang tiền mặt COD")
    public ApiResponse<PaymentResponse> switchToCod(@PathVariable Long orderId) {
        log.info("Nhận yêu cầu chuyển đổi sang COD cho đơn hàng ID: {}", orderId);
        PaymentResponse response = paymentService.switchToCod(orderId);
        return ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Đã chuyển đổi phương thức thanh toán sang Tiền mặt (COD)")
                .data(response)
                .build();
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Lấy thông tin thanh toán của đơn hàng", description = "Truy xuất trạng thái thanh toán, mã QR và lịch sử giao dịch")
    public ApiResponse<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Lấy thông tin thanh toán thành công")
                .data(response)
                .build();
    }

    @PostMapping("/{orderId}/confirm-success")
    @Operation(summary = "Xác nhận thanh toán thành công", description = "Mô phỏng Webhook ngân hàng hoặc quản trị viên xác nhận giao dịch thành công")
    public ApiResponse<PaymentResponse> confirmPaymentSuccess(
            @PathVariable Long orderId,
            @RequestBody(required = false) ConfirmPaymentRequest request) {
        String ref = request != null ? request.getTransactionRef() : null;
        log.info("Xác nhận thanh toán cho đơn hàng ID: {} với Ref: {}", orderId, ref);
        PaymentResponse response = paymentService.confirmPaymentSuccess(orderId, ref);
        return ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Thanh toán đơn hàng đã được xác nhận thành công")
                .data(response)
                .build();
    }
}

