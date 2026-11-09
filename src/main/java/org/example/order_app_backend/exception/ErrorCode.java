package org.example.order_app_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Hệ thống & Chung
    UNCATEGORIZED_EXCEPTION("INTERNAL_ERROR", "Lỗi máy chủ không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY("INVALID_KEY", "Khóa định danh không hợp lệ", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED("VALIDATION_FAILED", "Dữ liệu yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    NOT_FOUND("RESOURCE_NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu", HttpStatus.NOT_FOUND),
    UNAUTHORIZED("UNAUTHORIZED", "Chưa xác thực danh tính", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("ACCESS_DENIED", "Bạn không có quyền truy cập tài nguyên này", HttpStatus.FORBIDDEN),
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "Vi phạm quy tắc nghiệp vụ", HttpStatus.UNPROCESSABLE_CONTENT),

    // User / Auth
    USER_NOT_FOUND("USER_NOT_FOUND", "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "Người dùng đã tồn tại trong hệ thống", HttpStatus.CONFLICT),
    USER_BANNED("USER_BANNED", "Tài khoản của bạn đã bị khóa do vi phạm chính sách", HttpStatus.FORBIDDEN),

    // Store & Menu
    STORE_NOT_FOUND("STORE_NOT_FOUND", "Không tìm thấy thông tin quán ăn", HttpStatus.NOT_FOUND),
    STORE_INACTIVE("STORE_INACTIVE", "Quán ăn hiện đang đóng cửa", HttpStatus.BAD_REQUEST),
    MENU_ITEM_NOT_FOUND("MENU_ITEM_NOT_FOUND", "Không tìm thấy món ăn", HttpStatus.NOT_FOUND),
    MENU_ITEM_UNAVAILABLE("MENU_ITEM_UNAVAILABLE", "Món ăn hiện đã hết hàng", HttpStatus.BAD_REQUEST),

    // Order
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND),
    ORDER_EMPTY("ORDER_EMPTY", "Đơn hàng phải chứa ít nhất một món ăn", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS("INVALID_ORDER_STATUS", "Trạng thái đơn hàng không hợp lệ cho thao tác này", HttpStatus.UNPROCESSABLE_CONTENT),
    ORDER_CANNOT_BE_CANCELLED("ORDER_CANNOT_BE_CANCELLED", "Không thể hủy đơn hàng ở trạng thái hiện tại", HttpStatus.BAD_REQUEST),
    INVALID_PICKUP_CODE("INVALID_PICKUP_CODE", "Mã QR hoặc mã PIN đối soát nhận hàng không chính xác", HttpStatus.BAD_REQUEST),

    // Payment
    PAYMENT_FAILED("PAYMENT_FAILED", "Giao dịch thanh toán thất bại", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "Không tìm thấy giao dịch thanh toán", HttpStatus.NOT_FOUND),

    // Dispute
    DISPUTE_NOT_FOUND("DISPUTE_NOT_FOUND", "Không tìm thấy hồ sơ khiếu nại", HttpStatus.NOT_FOUND),
    DISPUTE_ALREADY_EXISTS("DISPUTE_ALREADY_EXISTS", "Đơn hàng này đã có hồ sơ khiếu nại", HttpStatus.CONFLICT),
    DISPUTE_EVIDENCE_REQUIRED("DISPUTE_EVIDENCE_REQUIRED", "Khiếu nại bắt buộc phải có ít nhất một ảnh bằng chứng", HttpStatus.BAD_REQUEST),
    REFUND_EXCEEDS_ORDER_TOTAL("REFUND_EXCEEDS_ORDER_TOTAL", "Số tiền yêu cầu hoàn không được vượt quá tổng giá trị đơn hàng", HttpStatus.BAD_REQUEST),

    // Review
    REVIEW_ALREADY_EXISTS("REVIEW_ALREADY_EXISTS", "Đơn hàng này đã được đánh giá trước đó", HttpStatus.CONFLICT),
    ORDER_NOT_DELIVERED("ORDER_NOT_DELIVERED", "Chỉ có thể đánh giá khi đơn hàng đã hoàn tất giao nhận", HttpStatus.BAD_REQUEST),

    // Notification
    NOTIFICATION_NOT_FOUND("NOTIFICATION_NOT_FOUND", "Không tìm thấy thông báo", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
