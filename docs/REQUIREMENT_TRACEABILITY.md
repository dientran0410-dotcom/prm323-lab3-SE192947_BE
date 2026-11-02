# MA TRẬN THEO VẾT YÊU CẦU (REQUIREMENT TRACEABILITY MATRIX)

- **Dự án:** Food Ordering App (PRM393 - Lab 3)
- **Mục đích:** Đảm bảo 100% các yêu cầu từ đề bài Lab 3, luồng nghiệp vụ thực tế và màn hình giao diện (S0 – S13) được ánh xạ rõ ràng sang Backend Modules, APIs, CSDL và Kế hoạch Kiểm thử.

---

## 1. Bảng Ma trận Theo vết Toàn diện (Full Traceability Matrix)

| Yêu cầu Lab 3 / Đồ án | Nghiệp vụ Food Ordering App | Màn hình | Backend Module | RESTful API | CSDL (MySQL Table) | Kế hoạch Test | Trạng thái |
|---|---|:---:|---|---|---|---|:---:|
| **REQ-FOUNDATION-00:** Nền tảng Kỹ thuật | Cấu hình MySQL, Validation, Uniform API/Error Response, Security Abstraction, Swagger | System | `Foundation` | `GET /api/v1/health`<br/>`POST /api/v1/health/test-validation` | MySQL `food_ordering_db` | `HealthControllerTest`<br/>`CurrentUserProviderTest` | `DONE` |
| **REQ-AUTH-01:** Xác thực Người dùng | Đăng nhập Google / SĐT, đồng bộ hồ sơ sinh viên KTX | S0, S0_Reg, S0_OTP | `User / Auth` | `POST /api/v1/auth/sync-user`<br/>`GET /api/v1/users/me` | `users` | `AuthServiceTest`<br/>`AuthControllerTest` | `DONE` |
| **REQ-STORE-01:** Khám phá & Tìm kiếm Quán | Tìm quán theo từ khóa món ăn, lọc theo Phân khu KTX (Khu A / Khu B) | S1 | `Store` | `GET /api/v1/stores`<br/>`GET /api/v1/stores/{id}` | `stores` | `StoreServiceTest`<br/>`StoreControllerTest` | `DONE` |
| **REQ-MENU-01:** Thực đơn & Tùy biến món | Xem danh mục món theo quán, chọn Size, Topping, ghi chú món ăn | S2, S3 | `MenuItem` | `GET /api/v1/stores/{id}/menu-items` | `menu_items` | `MenuItemServiceTest`<br/>`StoreControllerTest` | `DONE` |
| **REQ-ORDER-01:** Đặt món & Gom đơn KTX | Tạo đơn hàng gom ca KTX, tính chia phí ship, lưu Price Snapshot | S4 | `Order` | `POST /api/v1/orders` | `orders`<br/>`order_items` | `OrderServiceTest`<br/>`OrderControllerTest` | `DONE` |
| **REQ-ORDER-02:** Theo dõi Tiến trình Đơn | Xem trạng thái đơn thời gian thực, chi tiết món đã đặt | S6 | `Order` | `GET /api/v1/orders/{id}`<br/>`GET /api/v1/orders` | `orders`<br/>`order_items` | `OrderServiceTest`<br/>`OrderControllerTest` | `DONE` |
| **REQ-ORDER-03:** Hủy đơn Hợp lệ | Cho phép sinh viên hủy đơn khi quán chưa chuẩn bị (`PENDING`) | S6 | `Order` | `POST /api/v1/orders/{id}/cancel` | `orders` | `OrderServiceTest`<br/>`OrderControllerTest` | `DONE` |
| **REQ-PAY-01:** Thanh toán VietQR | Tự động sinh mã VietQR chuẩn Napas với nội dung gắn mã đơn | S5 | `Payment` | `POST /api/v1/payments/{orderId}/generate-qr` | `payments` | `PaymentServiceTest`<br/>`PaymentControllerTest` | `DONE` |
| **REQ-PAY-02:** Dự phòng Tiền mặt (COD) | Đổi hình thức thanh toán sang COD khi app ngân hàng lỗi | S5 | `Payment` | `POST /api/v1/payments/{orderId}/switch-to-cod` | `payments`<br/>`orders` | `PaymentServiceTest`<br/>`PaymentControllerTest` | `DONE` |
| **REQ-PICKUP-01:** Nhận hàng tại Cổng KTX | Xuất mã QR đối soát và mã PIN (4 số cuối SĐT) tại trụ giao nhận | S7 | `Order` | `GET /api/v1/orders/{id}` | `orders` | `OrderServiceTest`<br/>`OrderControllerTest` | `DONE` |
| **REQ-PICKUP-02:** Xác nhận Giao thành công | Shipper quét QR hoặc nhập PIN đối soát $\rightarrow$ Đổi đơn sang `DELIVERED` | S7 | `Order` | `POST /api/v1/orders/{id}/confirm-pickup` | `orders` | `OrderServiceTest`<br/>`OrderControllerTest` | `DONE` |
| **REQ-DISPUTE-01:** Báo cáo Sự cố & Khiếu nại | Sinh viên khiếu nại món hỏng/thiếu, bắt buộc có ảnh bằng chứng | S8 | `Dispute` | `POST /api/v1/disputes` | `disputes` | `DisputeServiceTest`<br/>`DisputeControllerTest` | `DONE` |
| **REQ-DISPUTE-02:** Thẩm định & Lịch sử Hoàn tiền | Kiểm tra giới hạn số tiền hoàn, theo dõi tiến trình xử lý | S8b, S8c | `Dispute` | `GET /api/v1/disputes/{id}`<br/>`GET /api/v1/disputes` | `disputes` | `DisputeServiceTest`<br/>`DisputeControllerTest` | `DONE` |
| **REQ-REVIEW-01:** Đánh giá Quán & Shipper | Chấm sao từ 1-5 và nhận xét sau khi đơn hàng hoàn tất | S9 | `Review` | `POST /api/v1/reviews`<br/>`GET /api/v1/stores/{storeId}/reviews` | `reviews`<br/>`stores` | `ReviewServiceTest`<br/>`ReviewControllerTest` | `DONE` |
| **REQ-NOTI-01:** Thông báo Tiến trình | Lưu vết và cung cấp thông báo khi đơn hàng đổi trạng thái | S12 | `Notification` | `GET /api/v1/notifications`<br/>`PATCH /api/v1/notifications/{id}/read`<br/>`GET /api/v1/notifications/unread-count` | `notifications` | `NotificationServiceTest`<br/>`NotificationControllerTest` | `DONE` |

---

## 2. Tiêu chí Đánh giá Môn học PRM393 Liên quan Backend
1. **Tính độc lập & Clean Architecture:** Tầng Service không bị ràng buộc chặt vào MySQL để sẵn sàng tích hợp Cloud Firestore.
2. **Xác thực định dạng & Ràng buộc nghiệp vụ (Validation):** Không để lỗi dữ liệu gây crash hệ thống; lỗi trả về mã lỗi và thông báo trực quan theo chuẩn HTTP status.
3. **Price Snapshot:** Không làm sai lệch giá tiền đơn hàng cũ khi cập nhật thực đơn.
4. **Đối soát nhận hàng hai phương thức (QR & PIN):** Đảm bảo tính khả thi trong thực tế ngoài trời tại ký túc xá.
