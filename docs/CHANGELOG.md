# NHẬT KÝ THAY ĐỔI DỰ ÁN (PROJECT CHANGELOG)

- **Quy định ghi chép:** Mọi task hoàn thành đều bắt buộc phải cập nhật nhật ký tại file này với đầy đủ các mục chi tiết.

---

## 2026-11-02 – FLOW 9: NOTIFICATION CENTER (COMPLETED)

### Task
Triển khai Flow 9 – Trung tâm Thông báo Đơn hàng, Quản lý Trạng thái Đã đọc & Sẵn sàng Push FCM (Notification Center Module)

### Overview
- Thiết kế và triển khai thực thể JPA & Enums:
  - `NotificationType`: Enum (`ORDER_STATUS`, `DISPUTE_UPDATE`, `PAYMENT_UPDATE`, `SYSTEM`).
  - `NotificationEntity`: Bảng `notifications` (lưu trữ `user_id`, `title`, `body`, `type`, `is_read`, `data_payload`, `created_at`, có index trên `user_id` và `created_at`).
- Triển khai Repository Abstraction:
  - Pure Domain Interface: `NotificationRepository`.
  - Spring Data JPA: `NotificationJpaRepository`.
  - Implementation: `MySqlNotificationRepository` (`@Primary`).
- Xây dựng DTOs & Mappers:
  - `NotificationResponse`.
  - `UnreadNotificationCountResponse`.
  - `NotificationMapper`.
- Xây dựng Business Services (`NotificationService` & `NotificationServiceImpl`):
  - `BR-NOTI-01`: Quyền sở hữu thông báo - Sinh viên chỉ có quyền xem và đánh dấu đã đọc các thông báo của chính mình (`user_id == currentUserId`). Bất kỳ truy cập trái phép nào đều bị từ chối với `ACCESS_DENIED` (HTTP 403).
  - `BR-NOTI-02`: Đổi trạng thái đã đọc - Cho phép đánh dấu đã đọc từng thông báo (`markAsRead`) hoặc đánh dấu tất cả thông báo của sinh viên (`markAllAsRead`).
  - `BR-NOTI-02b`: Đếm nhanh thông báo chưa đọc (`getUnreadCount`) phục vụ hiển thị badge số lượng thông báo mới trên Flutter UI icon chuông.
  - `BR-NOTI-03`: Tự động lưu thông báo theo sự kiện (`sendNotification`) và chuẩn bị sẵn kết nối Firebase Cloud Messaging (FCM).
- Xây dựng Controllers (`NotificationController`):
  - `GET /api/v1/notifications` (API-019): Lấy danh sách lịch sử thông báo của sinh viên (phân trang, sắp xếp theo `createdAt` DESC, HTTP 200 OK).
  - `PATCH /api/v1/notifications/{id}/read` (API-020): Đánh dấu một thông báo là đã đọc (HTTP 200 OK).
  - `PATCH /api/v1/notifications/read-all` (API-021): Đánh dấu toàn bộ thông báo của người dùng là đã đọc (HTTP 200 OK).
  - `GET /api/v1/notifications/unread-count` (API-022): Đếm số lượng thông báo chưa đọc (HTTP 200 OK).
- Viết Unit Tests & Integration Tests:
  - `NotificationServiceTest`: 7 unit tests bao phủ toàn bộ các phương thức nghiệp vụ (lấy danh sách phân trang, đánh dấu đã đọc thành công, từ chối khi không tìm thấy, từ chối khi không phải chủ sở hữu, đánh dấu tất cả, đếm số chưa đọc, lưu thông báo mới).
  - `NotificationControllerTest`: 4 integration tests kiểm tra toàn diện API-019, API-020, API-021, API-022 qua MockMvc.
  - Toàn bộ test suite chạy thành công: **84/84 tests passed (100%)**.

### Files Created
- `org.example.order_app_backend.entity.NotificationType`
- `org.example.order_app_backend.entity.NotificationEntity`
- `org.example.order_app_backend.repository.NotificationRepository`
- `org.example.order_app_backend.repository.mysql.jpa.NotificationJpaRepository`
- `org.example.order_app_backend.repository.mysql.MySqlNotificationRepository`
- `org.example.order_app_backend.dto.response.NotificationResponse`
- `org.example.order_app_backend.dto.response.UnreadNotificationCountResponse`
- `org.example.order_app_backend.mapper.NotificationMapper`
- `org.example.order_app_backend.service.NotificationService`
- `org.example.order_app_backend.service.impl.NotificationServiceImpl`
- `org.example.order_app_backend.controller.NotificationController`
- `src/test/java/org/example/order_app_backend/service/NotificationServiceTest.java`
- `src/test/java/org/example/order_app_backend/controller/NotificationControllerTest.java`

### Files Modified
- `org.example.order_app_backend.exception.ErrorCode` (bổ sung `NOTIFICATION_NOT_FOUND`)
- `docs/BUSINESS_RULES.md` (bổ sung Mục 6 - `BR-NOTI-01`, `BR-NOTI-02`, `BR-NOTI-03`)
- `docs/API_CATALOG.md` (bổ sung đặc tả API-019, API-020, API-021, API-022)
- `docs/REQUIREMENT_TRACEABILITY.md` (đánh dấu `REQ-NOTI-01` sang `DONE`)
- `docs/PROJECT_CONTEXT.md` (cập nhật Flow 9 HOÀN THÀNH, 9 bảng active, 84/84 tests pass)

### Verification
- `mvn test` trong module `order_app_backend`: 84/84 tests passed (0 failures, 0 errors, 0 skipped).

---

## 2026-11-02 – FLOW 8: REVIEW & RATING (COMPLETED)

### Task
Triển khai Flow 8 – Đánh giá Món ăn Quán & Shipper, Tự động Tính toán Điểm Trung bình Quán ăn (Review & Rating Module)

### Overview
- Thiết kế và triển khai thực thể JPA:
  - `ReviewEntity`: Bảng `reviews` (lưu trữ `order_id` [UNIQUE], `user_id`, `store_id`, `store_rating` [1-5 sao], `shipper_rating` [1-5 sao], `comment`, `created_at`, `updated_at`).
- Triển khai Repository Abstraction:
  - Pure Domain Interface: `ReviewRepository`.
  - Spring Data JPA: `ReviewJpaRepository`.
  - Implementation: `MySqlReviewRepository` (`@Primary`).
- Xây dựng DTOs & Mappers:
  - `CreateReviewRequest` (Validation: `orderId` @NotNull, `storeRating` @NotNull @Min(1) @Max(5), `shipperRating` @NotNull @Min(1) @Max(5), `comment`).
  - `ReviewResponse`.
  - `ReviewMapper` (chuyển đổi Entity sang Response DTO kèm tên quán và tên hiển thị người đánh giá).
- Xây dựng Business Services (`ReviewService` & `ReviewServiceImpl`):
  - Ràng buộc trạng thái đơn hàng (`BR-REV-01`): Chỉ cho phép đánh giá khi đơn hàng đã hoàn tất giao (`status == DELIVERED`). Nếu chưa giao xong $\rightarrow$ ném `ORDER_NOT_DELIVERED` (HTTP 400).
  - Ràng buộc chủ sở hữu đơn hàng (`BR-REV-01`): Chỉ sinh viên đặt đơn mới có quyền gửi đánh giá. Nếu không phải chủ đơn $\rightarrow$ ném `ACCESS_DENIED` (HTTP 403).
  - Ràng buộc duy nhất (`BR-REV-01`): Mỗi đơn hàng chỉ được tạo tối đa 01 đánh giá (`existsByOrderId`). Nếu gửi lại $\rightarrow$ ném `REVIEW_ALREADY_EXISTS` (HTTP 409).
  - Ràng buộc thang điểm (`BR-REV-01`): Cả điểm quán (`storeRating`) và điểm shipper (`shipperRating`) phải từ 1 đến 5 sao.
  - Tự động cập nhật điểm trung bình của quán (`BR-REV-02`): Tính toán lại `ratingAvg = (oldAvg * oldCount + newRating) / (oldCount + 1)` làm tròn 2 chữ số thập phân (`RoundingMode.HALF_UP`), đồng thời tăng `ratingCount` lên 1 và lưu vào `StoreEntity`.
- Xây dựng Controllers (`ReviewController`):
  - `POST /api/v1/reviews` (API-017): Đánh giá quán & shipper sau khi nhận hàng (HTTP 201 Created).
  - `GET /api/v1/stores/{storeId}/reviews` (API-018): Lấy danh sách đánh giá của quán có phân trang (HTTP 200 OK).
  - `GET /api/v1/orders/{orderId}/review` (API-018b): Xem lại đánh giá đã gửi của đơn hàng (HTTP 200 OK).
- Viết Unit Tests & Integration Tests:
  - `ReviewServiceTest`: 4 unit tests bao phủ tạo đánh giá thành công kèm kiểm tra công thức tính lại ratingAvg của quán, từ chối khi đơn chưa DELIVERED, từ chối khi đơn đã có đánh giá, và lấy danh sách đánh giá phân trang.
  - `ReviewControllerTest`: 3 integration tests kiểm tra toàn diện API-017, API-018, API-018b qua MockMvc.
  - Toàn bộ test suite chạy thành công: **73/73 tests passed (100%)**.

### Files Created
- `org.example.order_app_backend.entity.ReviewEntity`
- `org.example.order_app_backend.repository.ReviewRepository`
- `org.example.order_app_backend.repository.mysql.jpa.ReviewJpaRepository`
- `org.example.order_app_backend.repository.mysql.MySqlReviewRepository`
- `org.example.order_app_backend.dto.request.CreateReviewRequest`
- `org.example.order_app_backend.dto.response.ReviewResponse`
- `org.example.order_app_backend.mapper.ReviewMapper`
- `org.example.order_app_backend.service.ReviewService`
- `org.example.order_app_backend.service.impl.ReviewServiceImpl`
- `org.example.order_app_backend.controller.ReviewController`
- `src/test/java/org/example/order_app_backend/service/ReviewServiceTest.java`
- `src/test/java/org/example/order_app_backend/controller/ReviewControllerTest.java`

### Files Modified
- `org.example.order_app_backend.exception.ErrorCode` (bổ sung `REVIEW_ALREADY_EXISTS`, `REVIEW_NOT_FOUND`)
- `docs/API_CATALOG.md` (cập nhật API-017, bổ sung API-018, API-018b)
- `docs/REQUIREMENT_TRACEABILITY.md` (đánh dấu `REQ-REVIEW-01` sang `DONE`)
- `docs/PROJECT_CONTEXT.md` (cập nhật Flow 8 HOÀN THÀNH, 8 bảng active, 73/73 tests pass)

### Verification
- `mvn test` trong module `order_app_backend`: 73/73 tests passed (0 failures, 0 errors, 0 skipped).

---

## 2026-11-02 – FLOW 7: DISPUTE & REFUND (COMPLETED)

### Task
Triển khai Flow 7 – Khiếu nại Sự cố Đồ ăn, Ràng buộc Bằng chứng Hình ảnh & Thẩm định Hoàn tiền (Dispute & Refund Module)

### Overview
- Thiết kế và triển khai thực thể JPA:
  - `DisputeEntity`: Bảng `disputes` (lưu trữ `order_id` [UNIQUE], `user_id`, `reason` [Enum: `DAMAGED_FOOD`, `MISSING_ITEM`, `WRONG_ITEM`, `LATE_DELIVERY`, `OTHER`], `description`, `evidence_images` [JSON chuỗi URL ảnh từ Firebase Storage], `refund_requested_amount`, `refund_approved_amount`, `status` [Enum: `PENDING`, `INVESTIGATING`, `APPROVED`, `REJECTED`], `resolution_note`).
- Triển khai Repository Abstraction:
  - Pure Interface: `DisputeRepository`.
  - Spring Data JPA: `DisputeJpaRepository`.
  - Implementation: `MySqlDisputeRepository` (`@Primary`).
- Xây dựng DTOs & Mappers:
  - `CreateDisputeRequest` (Validation: `orderId` @NotNull, `reason` @NotNull, `description` @NotBlank, `evidenceImages` @NotEmpty, `refundRequestedAmount` @NotNull @DecimalMin("1.0")).
  - `ResolveDisputeRequest` (`status` @NotNull, `refundApprovedAmount`, `resolutionNote`).
  - `DisputeResponse`.
  - `DisputeMapper` (chuyển đổi Entity + giải mã an toàn mảng JSON `evidence_images`).
- Xây dựng Business Services (`DisputeService` & `DisputeServiceImpl`):
  - Ràng buộc bằng chứng bắt buộc (`BR-DIS-02`): Bắt buộc đính kèm tối thiểu 01 URL ảnh chụp thực tế bằng chứng món ăn hỏng/thiếu/đổ vỡ. Nếu rỗng $\rightarrow$ ném `DISPUTE_EVIDENCE_REQUIRED` (HTTP 400).
  - Ràng buộc trạng thái đơn hàng (`BR-DIS-01`): Chỉ cho phép khiếu nại đơn đã giao thành công (`status == DELIVERED`). Nếu chưa giao xong $\rightarrow$ ném `ORDER_NOT_DELIVERED` (HTTP 400).
  - Ràng buộc duy nhất (`BR-DIS-01`): Mỗi đơn hàng chỉ được tạo tối đa 01 khiếu nại. Nếu trùng $\rightarrow$ ném `DISPUTE_ALREADY_EXISTS` (HTTP 409).
  - Ràng buộc thời hạn (`BR-DIS-01`): Chỉ được khiếu nại trong vòng 24 giờ kể từ thời điểm nhận hàng (`deliveredAt`).
  - Thẩm định số tiền hoàn (`BR-DIS-03`): Kiểm tra $0 < \text{refundRequestedAmount} \le \text{finalAmount}$. Nếu vượt quá $\rightarrow$ ném `REFUND_EXCEEDS_ORDER_TOTAL` (HTTP 400).
  - Phê duyệt hoàn tiền (`resolveDispute`): Cập nhật `refundApprovedAmount`, tự động chuyển trạng thái thanh toán của đơn sang `REFUNDED` nếu hoàn toàn bộ giá trị.
  - Phân quyền dữ liệu (`BR-SEC-01`): Sinh viên chỉ được tạo và xem khiếu nại của chính mình; Quản trị viên/Chủ quán có quyền thẩm định.
- Xây dựng Controllers (`DisputeController`):
  - `POST /api/v1/disputes` (API-014): Gửi khiếu nại & yêu cầu hoàn tiền (HTTP 201 Created).
  - `GET /api/v1/disputes/{id}` (API-015): Xem chi tiết tiến trình khiếu nại (HTTP 200 OK).
  - `GET /api/v1/disputes` (API-016): Lấy danh sách lịch sử khiếu nại của sinh viên (HTTP 200 OK).
  - `PATCH /api/v1/disputes/{id}/resolve` (API-015b): Thẩm định phê duyệt hoặc từ chối khiếu nại (HTTP 200 OK).
- Viết Unit Tests & Integration Tests:
  - `DisputeServiceTest`: 6 unit tests bao phủ tạo khiếu nại thành công, từ chối khi thiếu ảnh, từ chối khi đơn chưa giao, từ chối khi tiền hoàn vượt quá tổng tiền đơn, từ chối khiếu nại trùng, và thẩm định APPROVED.
  - `DisputeControllerTest`: 5 integration tests kiểm tra toàn diện API-014, API-015, API-016, API-015b qua MockMvc.
  - Toàn bộ test suite chạy thành công: **66/66 tests passed (100%)**.

### Files Created
- `org.example.order_app_backend.entity.DisputeReason`
- `org.example.order_app_backend.entity.DisputeStatus`
- `org.example.order_app_backend.entity.DisputeEntity`
- `org.example.order_app_backend.repository.DisputeRepository`
- `org.example.order_app_backend.repository.mysql.jpa.DisputeJpaRepository`
- `org.example.order_app_backend.repository.mysql.MySqlDisputeRepository`
- `org.example.order_app_backend.dto.request.CreateDisputeRequest`
- `org.example.order_app_backend.dto.request.ResolveDisputeRequest`
- `org.example.order_app_backend.dto.response.DisputeResponse`
- `org.example.order_app_backend.mapper.DisputeMapper`
- `org.example.order_app_backend.service.DisputeService`
- `org.example.order_app_backend.service.impl.DisputeServiceImpl`
- `org.example.order_app_backend.controller.DisputeController`
- `src/test/java/org/example/order_app_backend/service/DisputeServiceTest.java`
- `src/test/java/org/example/order_app_backend/controller/DisputeControllerTest.java`

### Verification
- `mvn clean test`: 66/66 tests passed, 0 failures, 0 errors.

---

## 2026-11-02 – FLOW 6: ORDER PICKUP & FULFILLMENT AT GATE (COMPLETED)

### Task
Triển khai Flow 6 – Đối soát Nhận hàng tại Cổng KTX bằng QR Scan / PIN Fallback & Quản lý Tiến trình Đơn hàng (Order Pickup & Fulfillment Module)

### Overview
- Mở rộng thực thể JPA:
  - `OrderEntity`: Bổ sung trường `deliveredAt` (thời điểm giao nhận thành công), phục vụ ràng buộc thời hạn gửi khiếu nại (BR-DIS-01: trong vòng 24 giờ).
- Xây dựng DTOs & Mappers:
  - `UpdateOrderStatusRequest`: DTO cập nhật trạng thái đơn theo state machine (`PREPARING`, `READY`, `DELIVERING`, `DELIVERED`, `CANCELLED`).
  - `ConfirmPickupRequest`: DTO đối soát nhận hàng (`verificationMethod`: `QR_SCAN` hoặc `PIN`, `code`: mã hash QR hoặc mã PIN 4 số cuối SĐT).
  - `PickupConfirmationResponse`: DTO phản hồi xác nhận nhận hàng thành công (`orderId`, `status`: `DELIVERED`, `deliveredAt`).
  - Cập nhật `OrderResponse` và `OrderMapper` ánh xạ `deliveredAt`.
- Xây dựng Business Services:
  - `confirmPickup(Long orderId, ConfirmPickupRequest request)`:
    - Kiểm tra trạng thái đơn: Bắt buộc đơn phải ở trạng thái `DELIVERING` hoặc `READY` (`BR-ORD-06`). Nếu không hợp lệ $\rightarrow$ ném `INVALID_ORDER_STATUS` (HTTP 422).
    - Đối soát mã QR & PIN (`BR-ORD-05`): Khớp mã QR hoặc mã PIN 4 số. Nếu sai $\rightarrow$ ném `INVALID_PICKUP_CODE` (HTTP 400).
    - Luồng tiền mặt COD: Khi xác nhận nhận hàng COD, hệ thống tự động cập nhật `paymentStatus = PAID`.
    - Ghi nhận `deliveredAt = Instant.now()`, chuyển `status = DELIVERED`.
  - `updateOrderStatus(Long orderId, UpdateOrderStatusRequest request)`:
    - Bảo vệ tính toàn vẹn State Machine (`BR-ORD-06`): Chặn chuyển đổi trạng thái khi đơn đã ở trạng thái kết thúc (`DELIVERED` hoặc `CANCELLED`).
    - Hỗ trợ quán ăn và shipper chuyển đổi tiến trình: `PREPARING` $\rightarrow$ `READY` $\rightarrow$ `DELIVERING`.
- Xây dựng Controllers:
  - `PATCH /api/v1/orders/{orderId}/status` (API-010): Quán ăn / Shipper cập nhật trạng thái đơn hàng (HTTP 200 OK).
  - `POST /api/v1/orders/{orderId}/confirm-pickup` (API-011): Sinh viên xuất trình QR / PIN tại trụ KTX, xác nhận hoàn tất giao hàng (HTTP 200 OK).
- Viết Unit Tests & Integration Tests:
  - `OrderServiceTest`: 11 tests (+5 tests mới bao phủ QR scan thành công, PIN fallback thành công, sai mã đối soát, sai trạng thái, và tiến trình status).
  - `OrderControllerTest`: 7 tests (+2 integration tests kiểm tra toàn diện API-010 và API-011).
  - Toàn bộ test suite chạy thành công: **55/55 tests passed (100%)**.

### Files Created
- `org.example.order_app_backend.dto.request.UpdateOrderStatusRequest`
- `org.example.order_app_backend.dto.request.ConfirmPickupRequest`
- `org.example.order_app_backend.dto.response.PickupConfirmationResponse`

### Files Modified
- `org.example.order_app_backend.entity.OrderEntity` (Thêm `deliveredAt`)
- `org.example.order_app_backend.dto.response.OrderResponse` (Thêm `deliveredAt`)
- `org.example.order_app_backend.mapper.OrderMapper` (Ánh xạ `deliveredAt`)
- `org.example.order_app_backend.service.OrderService` (Khai báo `updateOrderStatus`, `confirmPickup`)
- `org.example.order_app_backend.service.impl.OrderServiceImpl` (Triển khai logic đối soát và state machine)
- `org.example.order_app_backend.controller.OrderController` (Thêm endpoint API-010, API-011)
- `src/test/java/org/example/order_app_backend/service/OrderServiceTest.java` (Thêm 5 tests)
- `src/test/java/org/example/order_app_backend/controller/OrderControllerTest.java` (Thêm 2 tests)

### Verification
- `mvn clean test`: 55/55 tests passed, 0 failures, 0 errors.

---

## 2026-11-02 – FLOW 5: PAYMENT & COD FALLBACK (COMPLETED)

### Task
Triển khai Flow 5 – Thanh toán VietQR Napas 247 & Cơ chế Dự phòng Chuyển sang COD (Payment & COD Fallback Module)

### Overview
- Thiết kế và triển khai thực thể JPA:
  - `PaymentEntity`: Bảng `payments` (lưu trữ `order_id` [UNIQUE], `payment_method`, `amount`, `status`, `transaction_ref` [UNIQUE], `vietqr_content`, `qr_url`, `paid_at`).
- Triển khai Repository Abstraction:
  - Pure Interface: `PaymentRepository`.
  - Spring Data JPA: `PaymentJpaRepository`.
  - Implementation: `MySqlPaymentRepository` (`@Primary`).
- Triển khai Gateway Abstraction:
  - `PaymentGateway` interface & `PaymentQrResult`.
  - `VietQrPaymentGateway`: Sinh liên kết ảnh mã VietQR chuẩn Napas 247 QuickLink (MBBank, bin 970422, TK 0987654321), gắn nội dung chuyển khoản bắt buộc `ORD_<ORDER_ID>` theo quy định `BR-PAY-01`.
- Xây dựng DTOs & Mappers:
  - `PaymentResponse`, `ConfirmPaymentRequest`.
  - `PaymentMapper`: Chuyển đổi an toàn Entity + QR result sang Response DTO.
- Xây dựng Business Service (`PaymentService` & `PaymentServiceImpl`):
  - `generatePaymentQr(Long orderId)`: Sinh mã VietQR, khởi tạo/cập nhật bản ghi `payments`, chuyển trạng thái phương thức đơn hàng sang `VIETQR`.
  - `switchToCod(Long orderId)` (Cơ chế Dự phòng `BR-PAY-02`): Chuyển đơn hàng sang `COD`, trạng thái thanh toán `UNPAID`, cập nhật trạng thái đơn `PENDING` sang `CONFIRMED`.
  - `confirmPaymentSuccess(Long orderId, String transactionRef)`: Mô phỏng xác nhận thanh toán thành công, ghi nhận mã giao dịch `transaction_ref`, chuyển trạng thái đơn hàng sang `CONFIRMED` và `PAID`.
  - `getPaymentByOrderId(Long orderId)`: Truy xuất thông tin thanh toán của đơn hàng.
  - Phân quyền sở hữu dữ liệu (`BR-SEC-01`): Chỉ sinh viên chủ đơn hoặc Quản trị viên mới được thao tác.
- Xây dựng Controller (`PaymentController`):
  - `POST /api/v1/payments/{orderId}/generate-qr` (API-012): Sinh link và mã VietQR thanh toán.
  - `POST /api/v1/payments/{orderId}/switch-to-cod` (API-013): Đổi phương thức thanh toán sang Tiền mặt (COD Fallback).
  - `GET /api/v1/payments/{orderId}`: Lấy thông tin thanh toán của đơn hàng.
  - `POST /api/v1/payments/{orderId}/confirm-success`: Xác nhận thanh toán thành công.
- Viết Unit Tests & Integration Tests:
  - `PaymentServiceTest`: 5 unit tests kiểm tra sinh VietQR, kiểm tra mã nội dung `ORD_<ID>`, cơ chế dự phòng COD, từ chối COD khi đơn đã DELIVERED, và xác nhận thanh toán.
  - `PaymentControllerTest`: 4 integration tests qua MockMvc kiểm tra toàn diện API-012, API-013, truy xuất thanh toán và callback xác nhận thành công.
  - Toàn bộ test suite chạy thành công: **48/48 tests passed (100%)**.

### Files Created
- `org.example.order_app_backend.entity.PaymentEntity`
- `org.example.order_app_backend.repository.PaymentRepository`
- `org.example.order_app_backend.repository.mysql.jpa.PaymentJpaRepository`
- `org.example.order_app_backend.repository.mysql.MySqlPaymentRepository`
- `org.example.order_app_backend.service.gateway.PaymentGateway`
- `org.example.order_app_backend.service.gateway.PaymentQrResult`
- `org.example.order_app_backend.service.gateway.VietQrPaymentGateway`
- `org.example.order_app_backend.dto.response.PaymentResponse`
- `org.example.order_app_backend.dto.request.ConfirmPaymentRequest`
- `org.example.order_app_backend.mapper.PaymentMapper`
- `org.example.order_app_backend.service.PaymentService`
- `org.example.order_app_backend.service.impl.PaymentServiceImpl`
- `org.example.order_app_backend.controller.PaymentController`
- `src/test/java/org/example/order_app_backend/service/PaymentServiceTest.java`
- `src/test/java/org/example/order_app_backend/controller/PaymentControllerTest.java`

### Verification
- `mvn clean test`: 48/48 tests passed, 0 failures, 0 errors.

---

## 2026-11-02 – FLOW 3 & 4: CART, CHECKOUT & ORDER CREATION (COMPLETED)

### Task
Triển khai Flow 3 & 4 – Giỏ hàng, Đặt món Gom đơn KTX & Khởi tạo Đơn hàng (Cart, Checkout & Order Creation Module)

### Overview
- Thiết kế và triển khai thực thể JPA:
  - `OrderEntity`: Bảng `orders` (lưu trữ mã đơn `order_code`, `user_id`, `store_id`, ca gom đơn `slot_time`, điểm nhận hàng KTX `pickup_location`, các khoản tiền `subtotal_amount`, `delivery_fee`, `group_discount_amount`, `final_amount`, trạng thái đơn `status`, phương thức thanh toán `payment_method`, trạng thái thanh toán `payment_status`, mã đối soát `pickup_qr_code`, `pickup_pin`, và ghi chú).
  - `OrderItemEntity`: Bảng `order_items` (lưu trữ chi tiết từng món ăn với **Price Snapshot** bất biến: `item_name`, `unit_price`, số lượng `quantity`, tùy chọn `selected_options` JSON, và tổng tiền món `item_total_amount`).
  - Enums: `OrderStatus` (`PENDING`, `CONFIRMED`, `PREPARING`, `READY`, `DELIVERING`, `DELIVERED`, `CANCELLED`), `PaymentMethod` (`VIETQR`, `COD`), `PaymentStatus` (`UNPAID`, `PAID`, `REFUNDED`).
- Triển khai Repository Abstraction ("MySQL Now, Firebase Later"):
  - Pure Interfaces: `OrderRepository` & `OrderItemRepository`.
  - Spring Data JPA: `OrderJpaRepository` & `OrderItemJpaRepository`.
  - Implementation: `MySqlOrderRepository` & `MySqlOrderItemRepository` (`@Primary`).
  - Mở rộng `MenuItemRepository` hỗ trợ `findByIdIn(List<Long> ids)`.
- Xây dựng DTOs & Mappers:
  - Request DTOs: `SelectedItemOptionDto`, `OrderItemRequest`, `CreateOrderRequest`.
  - Response DTOs: `OrderItemResponse`, `OrderResponse`.
  - Mapper: `OrderMapper` (chuyển đổi Entity sang Response DTO kèm deserialize JSON selected options, snapshot immutability).
- Xây dựng Business Service (`OrderService` & `OrderServiceImpl`):
  - Kiểm tra giỏ hàng không rỗng (`BR-ORD-01`, mã lỗi `ORDER_EMPTY`).
  - Kiểm tra quán ăn tồn tại và đang mở cửa (`BR-ORD-01`, mã lỗi `STORE_NOT_FOUND`, `STORE_INACTIVE`).
  - Kiểm tra món ăn tồn tại và đang bán (`BR-ORD-01`, mã lỗi `MENU_ITEM_NOT_FOUND`, `MENU_ITEM_UNAVAILABLE`).
  - Thực hiện Price Snapshot bất biến (`BR-ORD-02`): chốt cứng đơn giá gốc và phụ thu tùy biến vào `order_items`, không bao giờ tính lại theo menu items sau khi tạo đơn.
  - Tính phí giao hàng gom đơn KTX (`BR-ORD-03`): phí gom đơn 5,000 VND cho các ca gom KTX (chuẩn lẻ 15,000 VND).
  - Tự động sinh mã đơn `orderCode` (`KTX-YYYYMMDD-XXXX`), mã QR hash nhận hàng `pickupQrCode` (`QR_KTX_...`), và mã PIN 4 số `pickupPin` (`BR-ORD-05`).
  - Triển khai State Machine hủy đơn (`BR-ORD-06`): cho phép sinh viên tự hủy đơn khi trạng thái là `PENDING`, từ chối hủy nếu trạng thái khác (`ORDER_CANNOT_BE_CANCELLED`).
  - Kiểm tra quyền truy cập dữ liệu (`BR-SEC-01`): sinh viên chỉ xem/hủy đơn của chính mình.
- Xây dựng Controller (`OrderController`):
  - `POST /api/v1/orders` (API-006): Tạo đơn hàng gom đơn KTX (HTTP 201 Created).
  - `GET /api/v1/orders/{id}` (API-007): Lấy chi tiết đơn hàng & theo dõi kèm price snapshot (HTTP 200 OK).
  - `GET /api/v1/orders` (API-008): Lấy lịch sử đơn hàng của sinh viên hiện tại (HTTP 200 OK).
  - `POST /api/v1/orders/{id}/cancel` (API-009): Hủy đơn hàng đang chờ duyệt (HTTP 200 OK).
- Viết Unit Tests & Integration Tests:
  - `OrderServiceTest`: 6 unit tests bao phủ toàn bộ validation, snapshot calculation, status check, và cancel state machine.
  - `OrderControllerTest`: 5 integration tests với MockMvc kiểm tra toàn diện API-006, API-007, API-008, API-009.
  - Toàn bộ test suite chạy thành công: **39/39 tests passed (100%)**.

### Files Created
- `org.example.order_app_backend.entity.OrderStatus`
- `org.example.order_app_backend.entity.PaymentMethod`
- `org.example.order_app_backend.entity.PaymentStatus`
- `org.example.order_app_backend.entity.OrderEntity`
- `org.example.order_app_backend.entity.OrderItemEntity`
- `org.example.order_app_backend.repository.OrderRepository`
- `org.example.order_app_backend.repository.OrderItemRepository`
- `org.example.order_app_backend.repository.mysql.jpa.OrderJpaRepository`
- `org.example.order_app_backend.repository.mysql.jpa.OrderItemJpaRepository`
- `org.example.order_app_backend.repository.mysql.MySqlOrderRepository`
- `org.example.order_app_backend.repository.mysql.MySqlOrderItemRepository`
- `org.example.order_app_backend.dto.request.SelectedItemOptionDto`
- `org.example.order_app_backend.dto.request.OrderItemRequest`
- `org.example.order_app_backend.dto.request.CreateOrderRequest`
- `org.example.order_app_backend.dto.response.OrderItemResponse`
- `org.example.order_app_backend.dto.response.OrderResponse`
- `org.example.order_app_backend.mapper.OrderMapper`
- `org.example.order_app_backend.service.OrderService`
- `org.example.order_app_backend.service.impl.OrderServiceImpl`
- `org.example.order_app_backend.controller.OrderController`
- `src/test/java/org/example/order_app_backend/service/OrderServiceTest.java`
- `src/test/java/org/example/order_app_backend/controller/OrderControllerTest.java`

### Files Modified
- `org.example.order_app_backend.repository.MenuItemRepository` (Thêm `findByIdIn`)
- `org.example.order_app_backend.repository.mysql.jpa.MenuItemJpaRepository` (Thêm `findByIdIn`)
- `org.example.order_app_backend.repository.mysql.MySqlMenuItemRepository` (Implement `findByIdIn`)

### Verification
- `mvn clean test`: 39/39 tests passed, 0 failures, 0 errors.

---

## 2026-11-02 – FLOW 2: STORE + MENU (COMPLETED)

### Task
Triển khai Flow 2 – Quán ăn & Thực đơn KTX (Store & Menu Management Module)

### Overview
- Thiết kế và triển khai thực thể JPA:
  - `StoreEntity`: Bảng `stores` (quán ăn liên kết KTX, phân khu `campus_zone`, giờ mở cửa, rating trung bình).
  - `MenuItemEntity`: Bảng `menu_items` (món ăn, danh mục, đơn giá gốc, tình trạng còn hàng, cấu hình tùy biến Size/Topping lưu chuỗi JSON).
- Triển khai Repository Abstraction:
  - `StoreRepository` & `MenuItemRepository` (Pure Java interfaces).
  - `StoreJpaRepository` & `MenuItemJpaRepository` (Spring Data JPA).
  - `MySqlStoreRepository` & `MySqlMenuItemRepository` (Primary implementation).
- Xây dựng DTOs & Mappers:
  - `CreateStoreRequest`, `UpdateStoreRequest`, `StoreResponse`, `StoreMapper`.
  - `CreateMenuItemRequest`, `UpdateMenuItemRequest`, `MenuItemResponse`, `CustomizationGroupDto`, `CustomizationOptionDto`, `MenuItemMapper` (xử lý an toàn JSON Customization Options qua Jackson).
- Xây dựng Business Services:
  - `StoreService` & `StoreServiceImpl`: Tìm kiếm quán ăn theo từ khóa, lọc theo phân khu KTX (Khu A / Khu B), phân trang qua `PageResponse<StoreResponse>`.
  - `MenuItemService` & `MenuItemServiceImpl`: Lấy danh sách thực đơn theo quán, lọc theo danh mục hoặc tình trạng còn hàng; kiểm tra ràng buộc quán tồn tại trước khi thêm món.
- Xây dựng Controllers:
  - `StoreController`: `GET /api/v1/stores`, `GET /api/v1/stores/{id}`, `POST /api/v1/stores`, `PUT /api/v1/stores/{id}`.
  - `MenuItemController`: `GET /api/v1/stores/{storeId}/menu-items`, `GET /api/v1/menu-items/{id}`, `POST /api/v1/stores/{storeId}/menu-items`, `PUT /api/v1/menu-items/{id}`.
- Cơ chế Dữ liệu Khởi tạo:
  - `DataInitializer`: Tự động nạp 2 quán ăn mẫu tại KTX Khu A & Khu B kèm 3 món ăn thực tế (Cơm Tấm B2, Bún Bò Huế Khu A) có đầy đủ nhóm tùy biến (Size, Topping, độ cay).
- Viết Unit Tests (`StoreServiceTest`, `MenuItemServiceTest`) và Integration Tests (`StoreControllerTest`).

### Files Created
- `org.example.order_app_backend.entity.StoreEntity`
- `org.example.order_app_backend.entity.MenuItemEntity`
- `org.example.order_app_backend.repository.StoreRepository`
- `org.example.order_app_backend.repository.mysql.jpa.StoreJpaRepository`
- `org.example.order_app_backend.repository.mysql.MySqlStoreRepository`
- `org.example.order_app_backend.repository.MenuItemRepository`
- `org.example.order_app_backend.repository.mysql.jpa.MenuItemJpaRepository`
- `org.example.order_app_backend.repository.mysql.MySqlMenuItemRepository`
- `org.example.order_app_backend.dto.request.CreateStoreRequest`
- `org.example.order_app_backend.dto.request.UpdateStoreRequest`
- `org.example.order_app_backend.dto.response.StoreResponse`
- `org.example.order_app_backend.dto.request.CreateMenuItemRequest`
- `org.example.order_app_backend.dto.request.UpdateMenuItemRequest`
- `org.example.order_app_backend.dto.response.MenuItemResponse`
- `org.example.order_app_backend.dto.response.CustomizationGroupDto`
- `org.example.order_app_backend.dto.response.CustomizationOptionDto`
- `org.example.order_app_backend.mapper.StoreMapper`
- `org.example.order_app_backend.mapper.MenuItemMapper`
- `org.example.order_app_backend.service.StoreService`
- `org.example.order_app_backend.service.impl.StoreServiceImpl`
- `org.example.order_app_backend.service.MenuItemService`
- `org.example.order_app_backend.service.impl.MenuItemServiceImpl`
- `org.example.order_app_backend.controller.StoreController`
- `org.example.order_app_backend.controller.MenuItemController`
- `org.example.order_app_backend.config.DataInitializer`
- `src/test/.../service/StoreServiceTest.java`
- `src/test/.../service/MenuItemServiceTest.java`
- `src/test/.../controller/StoreControllerTest.java`

### APIs Added
- `GET /api/v1/stores`: Lấy danh sách & tìm kiếm quán ăn theo KTX / từ khóa (S1 Home).
- `GET /api/v1/stores/{id}`: Chi tiết quán ăn (S2 Store Detail).
- `POST /api/v1/stores`: Thêm quán ăn mới.
- `PUT /api/v1/stores/{id}`: Cập nhật quán ăn.
- `GET /api/v1/stores/{storeId}/menu-items`: Lấy thực đơn của quán (S2, S3).
- `GET /api/v1/menu-items/{id}`: Chi tiết món ăn kèm Size/Topping (S3).
- `POST /api/v1/stores/{storeId}/menu-items`: Thêm món vào thực đơn quán.
- `PUT /api/v1/menu-items/{id}`: Cập nhật thông tin món ăn.

### Database
- Bảng `stores` và `menu_items` đã được tạo trong MySQL với các chỉ mục:
  - `idx_stores_campus_zone`, `idx_stores_name`, `idx_stores_is_active`.
  - `idx_menu_items_store_id`, `idx_menu_items_category`, `idx_menu_items_name`, `idx_menu_items_is_available`.

### Tests
- **28/28 tests passed (100%)**:
  - `StoreServiceTest`: 5 unit tests pass.
  - `MenuItemServiceTest`: 4 unit tests pass.
  - `StoreControllerTest`: 6 integration tests pass.
  - `UserServiceTest`: 5 tests pass.
  - `AuthControllerTest`: 5 tests pass.
  - `HealthControllerTest`: 3 tests pass.
  - `CurrentUserProviderTest`: 2 tests pass.
  - `OrderAppBackendApplicationTests`: 1 test pass.

---

## 2026-11-02 – FLOW 1: USER / AUTHENTICATION (COMPLETED)

### Task
Triển khai Flow 1 – Người dùng & Xác thực (User / Authentication Module)

### Overview
- Thiết kế và triển khai thực thể JPA `UserEntity` ánh xạ với bảng `users` trong MySQL kèm các chỉ mục (`firebase_uid`, `email`, `phone`).
- Triển khai kiến trúc phân tầng chuẩn mực:
  - Repository interface thuần Java: `UserRepository`
  - Spring Data JPA interface: `UserJpaRepository`
  - Triển khai MySQL: `MySqlUserRepository` (Primary Bean)
- Xây dựng DTOs: `SyncUserRequest`, `UpdateUserProfileRequest`, `UserResponse`.
- Xây dựng `UserMapper` hỗ trợ chuyển đổi hai chiều và cập nhật từng phần (partial update).
- Xây dựng `UserService` và `UserServiceImpl`:
  - Đồng bộ tự động người dùng sau khi đăng nhập qua Google Sign-In hoặc OTP.
  - Tự động nhận diện tài khoản theo `firebaseUid` hoặc theo `email` để liên kết thông tin sinh viên KTX.
  - Lấy thông tin cá nhân `getCurrentUserProfile` dựa trên abstraction `CurrentUserProvider`.
  - Cập nhật thông tin phòng KTX, số điện thoại, ảnh đại diện.
- Xây dựng Controllers:
  - `AuthController`: `POST /api/v1/auth/sync-user`
  - `UserController`: `GET /api/v1/users/me`, `PATCH /api/v1/users/me`, `GET /api/v1/users/{id}`
- Viết Unit Tests (`UserServiceTest`) và Integration Tests (`AuthControllerTest`).

### Files Created
- `org.example.order_app_backend.entity.UserRole`
- `org.example.order_app_backend.entity.UserEntity`
- `org.example.order_app_backend.repository.UserRepository`
- `org.example.order_app_backend.repository.mysql.jpa.UserJpaRepository`
- `org.example.order_app_backend.repository.mysql.MySqlUserRepository`
- `org.example.order_app_backend.dto.request.SyncUserRequest`
- `org.example.order_app_backend.dto.request.UpdateUserProfileRequest`
- `org.example.order_app_backend.dto.response.UserResponse`
- `org.example.order_app_backend.mapper.UserMapper`
- `org.example.order_app_backend.service.UserService`
- `org.example.order_app_backend.service.impl.UserServiceImpl`
- `org.example.order_app_backend.controller.AuthController`
- `org.example.order_app_backend.controller.UserController`
- `src/test/.../service/UserServiceTest.java`
- `src/test/.../controller/AuthControllerTest.java`

### APIs Added
- `POST /api/v1/auth/sync-user`: Đồng bộ tài khoản sau khi đăng nhập Google hoặc OTP (S0, S0_Reg).
- `GET /api/v1/users/me`: Lấy hồ sơ người dùng hiện tại (S13).
- `PATCH /api/v1/users/me`: Cập nhật thông tin phòng KTX, số điện thoại (S13).
- `GET /api/v1/users/{id}`: Xem chi tiết người dùng theo ID.

### Database
- Bảng `users` trong MySQL được Hibernate tự động cập nhật schema với các ràng buộc khóa và index.

### Business Rules
- Bắt buộc kiểm tra định dạng email sinh viên.
- Nếu người dùng đã tồn tại `firebaseUid` hoặc `email`, thực hiện cập nhật và liên kết tài khoản thay vì ném lỗi trùng lặp.
- Chỉ người dùng đã xác thực mới được truy xuất và cập nhật hồ sơ cá nhân.

### Tests
- **13/13 tests pass (100%)**:
  - `UserServiceTest`: 5 unit tests pass.
  - `AuthControllerTest`: 5 integration tests pass.
  - `HealthControllerTest`: 3 tests pass.
  - `CurrentUserProviderTest`: 2 tests pass.
  - `OrderAppBackendApplicationTests`: 1 test pass.

---

## 2026-11-02 – FLOW 0: BACKEND FOUNDATION (COMPLETED)

### Task
Thiết lập Baseline Kiến trúc Backend, Đánh giá Hiện trạng & Hệ thống Tài liệu Ngữ cảnh (First Task - Architecture Baseline & Documentation Setup)

### Overview
- Đã quét toàn bộ mã nguồn hiện tại của dự án Backend Spring Boot (`order_app_backend`) và Frontend Flutter (`prm323-lab3-SE192947`).
- Xác định môi trường thực thi: Java 21 LTS, Spring Boot 4.1.2, Apache Maven 3.9.14.
- Đã thiết lập đầy đủ hệ thống 8 tài liệu ngữ cảnh trong `docs/` làm "Context Memory" để các phiên làm việc tiếp theo không phải quét lại toàn bộ mã nguồn.

### Files Created
- `docs/PROJECT_CONTEXT.md`: Tài liệu tổng quan ngữ cảnh và quyết định kiến trúc chính.
- `docs/ARCHITECTURE.md`: Tài liệu đặc tả phân lớp, sơ đồ luồng dữ liệu, repository abstraction (MySQL now, Firebase later).
- `docs/DATABASE_SCHEMA.md`: Đặc tả ERD và 9 bảng MySQL chuẩn hóa kèm kiểu dữ liệu, ràng buộc, index.
- `docs/API_CATALOG.md`: Đặc tả chi tiết 19 RESTful API endpoints với request, response, error codes, validation.
- `docs/BUSINESS_RULES.md`: Quy định toàn bộ luật nghiệp vụ (Price snapshot, State machine, QR/PIN đối soát, Dispute evidence).
- `docs/FIREBASE_MIGRATION.md`: Kế hoạch chi tiết ánh xạ MySQL sang Firestore, giải quyết JOIN, Auth, Cloud Storage.
- `docs/REQUIREMENT_TRACEABILITY.md`: Bảng ánh xạ theo vết từ yêu cầu Lab 3 sang API, CSDL, Test.
- `docs/CHANGELOG.md`: Nhật ký thay đổi baseline ban đầu.

### Code & Structure Changes
- Kiểm thử biên dịch thành công khung ứng dụng Spring Boot hiện tại: `mvn clean test-compile` -> `BUILD SUCCESS`.
- Cấu trúc package chuẩn phân lớp đã được rà soát:
  - `org.example.order_app_backend.controller`
  - `org.example.order_app_backend.service` (và `service.impl`)
  - `org.example.order_app_backend.repository`
  - `org.example.order_app_backend.entity`
  - `org.example.order_app_backend.dto` (`request`, `response`)
  - `org.example.order_app_backend.mapper`
  - `org.example.order_app_backend.exception`
  - `org.example.order_app_backend.config`
  - `org.example.order_app_backend.security`

### APIs Changed
- Chưa có code API nào bị sửa đổi (đã định nghĩa sẵn hợp đồng 19 API trong `docs/API_CATALOG.md`).

### Database Changed
- Chưa thay đổi CSDL thực tế (đã hoàn thiện tài liệu schema 9 bảng trong `docs/DATABASE_SCHEMA.md`).

### Business Rules Defined
- Đã quy định rõ các quy tắc nghiệp vụ bất biến: Price snapshot khi tạo đơn, State machine kiểm soát trạng thái đơn hàng, đối soát nhận hàng bằng QR/PIN, bắt buộc có ảnh khi gửi khiếu nại.

### Migration Impact
- Đã thiết kế Repository Abstraction Pattern để tầng Business Service không phụ thuộc vào `JpaRepository` hay Firebase SDK.

### Tests
- Biên dịch thành công Maven test-compile trên Java 21.

### Known Issues & Technical Debt
1. File `order_app_backend/src/main/resources/application.properties` hiện chưa có cấu hình kết nối CSDL MySQL (URL, username, password, JPA DDL-auto).
2. `order_app_backend/pom.xml` chưa khai báo dependency `spring-boot-starter-validation` phục vụ validation Jakarta (`@Valid`, `@NotNull`, `@Positive`).
3. Spring Security starter đã được nạp trong pom nhưng chưa có `SecurityConfig`, do đó mặc định Spring Security sẽ sinh password ngẫu nhiên và chặn các request nếu không được cấu hình mở/bỏ qua cho các API công khai.
