package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.request.CreateDisputeRequest;
import org.example.order_app_backend.dto.request.ResolveDisputeRequest;
import org.example.order_app_backend.dto.response.DisputeResponse;
import org.example.order_app_backend.service.DisputeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/disputes")
@RequiredArgsConstructor
@Tag(name = "Dispute Controller", description = "Các API khiếu nại sự cố đồ ăn, bắt buộc ảnh bằng chứng và xử lý hoàn tiền")
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gửi khiếu nại & yêu cầu hoàn tiền (API-014)", description = "Báo cáo sự cố đồ ăn, bắt buộc đính kèm ít nhất 01 URL ảnh bằng chứng, thẩm định số tiền hoàn")
    public ApiResponse<DisputeResponse> createDispute(@Valid @RequestBody CreateDisputeRequest request) {
        log.info("Nhận yêu cầu tạo khiếu nại cho đơn hàng ID: {}", request.getOrderId());
        DisputeResponse response = disputeService.createDispute(request);
        return ApiResponse.<DisputeResponse>builder()
                .success(true)
                .message("Gửi khiếu nại thành công, yêu cầu đang chờ thẩm định")
                .data(response)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết tiến trình khiếu nại (API-015)", description = "Lấy đầy đủ thông tin xử lý khiếu nại, ảnh bằng chứng và kết quả hoàn tiền")
    public ApiResponse<DisputeResponse> getDisputeById(@PathVariable Long id) {
        DisputeResponse response = disputeService.getDisputeById(id);
        return ApiResponse.<DisputeResponse>builder()
                .success(true)
                .message("Lấy thông tin khiếu nại thành công")
                .data(response)
                .build();
    }

    @GetMapping
    @Operation(summary = "Lịch sử khiếu nại của sinh viên (API-016)", description = "Danh sách tất cả các hồ sơ khiếu nại đã gửi của người dùng")
    public ApiResponse<List<DisputeResponse>> getMyDisputes() {
        List<DisputeResponse> disputes = disputeService.getMyDisputes();
        return ApiResponse.<List<DisputeResponse>>builder()
                .success(true)
                .message("Lấy lịch sử khiếu nại thành công")
                .data(disputes)
                .build();
    }

    @PatchMapping("/{id}/resolve")
    @Operation(summary = "Thẩm định và xử lý khiếu nại (Admin/Quán)", description = "Quản trị viên hoặc Quán ăn phê duyệt APPROVED (kèm số tiền hoàn) hoặc từ chối REJECTED")
    public ApiResponse<DisputeResponse> resolveDispute(
            @PathVariable Long id,
            @Valid @RequestBody ResolveDisputeRequest request) {
        log.info("Nhận yêu cầu thẩm định khiếu nại ID: {}", id);
        DisputeResponse response = disputeService.resolveDispute(id, request);
        return ApiResponse.<DisputeResponse>builder()
                .success(true)
                .message("Cập nhật kết quả thẩm định khiếu nại thành công")
                .data(response)
                .build();
    }
}

