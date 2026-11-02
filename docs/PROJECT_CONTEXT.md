# PROJECT CONTEXT – FOOD ORDERING APP (PRM393 LAB 3)

> **Mục đích:** Tài liệu này là "bộ nhớ ngữ cảnh" (Context Memory) cốt lõi của Backend. Mọi phiên làm việc tiếp theo của AI hoặc lập trình viên phải đọc tài liệu này đầu tiên để nắm toàn bộ trạng thái dự án mà không cần scan toàn bộ mã nguồn.

---

## 1. Tổng quan Dự án (Project Overview)
- **Tên dự án:** Food Ordering App – Ứng dụng Đặt món & Gom đơn KTX
- **Môn học:** PRM393 – Mobile Programming (Lab 3)
- **Mục tiêu hệ thống:** Cung cấp Backend RESTful API hoàn chỉnh phục vụ ứng dụng đặt đồ ăn cho sinh viên tại Ký túc xá (KTX Đại học Quốc gia / FPT). Hỗ trợ gom đơn theo ca trưa/tối để chia nhỏ phí vận chuyển, thanh toán VietQR / COD, đối soát nhận hàng bằng mã QR / PIN tại trụ KTX, đánh giá dịch vụ và khiếu nại hoàn tiền khi xảy ra sự cố.

---

## 2. Công nghệ & Môi trường (Tech Stack)
- **Ngôn ngữ:** Java 21 (JDK 21.0.9 LTS)
- **Framework:** Spring Boot 4.1.2 (Spring Framework 7.x, Spring Security 7.x)
- **Build Tool:** Apache Maven 3.9.14
- **ORM / Data Access Hiện tại:** Spring Data JPA / Hibernate (với MySQL Connector/J)
- **Cơ sở dữ liệu Hiện tại:** MySQL 8.x
- **Cơ sở dữ liệu Mục tiêu Tương lai:** Firebase Cloud Firestore (Google Cloud)
- **Tài liệu API:** OpenAPI 3 / Swagger (`springdoc-openapi-starter-webmvc-ui:3.2.0`)
- **Format API Contract:** JSON RESTful API (`/api/v1/...`), Response bọc chuẩn qua `ApiResponse<T>`, Error format qua `ApiErrorResponse`
- **Frontend Liên kết:** Flutter Mobile App (Kiến trúc MVVM + Provider tại `prm323-lab3-SE192947`)

---

## 3. Kiến trúc Cốt lõi: "MySQL Now, Firebase Later"
Để đảm bảo chuyển đổi mượt mà từ MySQL sang Cloud Firestore trong tương lai mà không sửa đổi Business Logic:
1. **Repository Abstraction:**
   - Business Service (`OrderService`, `StoreService`, ...) chỉ giao tiếp với Repository Interface thuần túy (`OrderRepository`, `StoreRepository`).
   - Tầng dữ liệu MySQL hiện tại triển khai qua `MySqlOrderRepository` (bọc Spring Data JPA `OrderJpaRepository`).
   - Tương lai: Triển khai `FirestoreOrderRepository` thay thế `MySqlOrderRepository` bằng cách đổi Bean / Profile cấu hình; `OrderService` giữ nguyên 100%.
2. **Không rò rỉ JPA ra ngoài Data Layer:**
   - Tuyệt đối không expose `@Entity` JPA ra Controller.
   - Không phụ thuộc vào Hibernate Lazy Loading trong tầng Service.
   - Luôn sử dụng DTO (`Request`, `Response`) và Mapper riêng biệt.
3. **Decoupled Authentication:**
   - Cung cấp abstraction `CurrentUserProvider` để lấy `userId`, `email`, `role`.
   - Hiện tại: Mock / Header-based / Local token context.
   - Tương lai: Firebase Auth Token filter giải mã Firebase UID và gán vào `CurrentUserProvider`.

---

## 4. Các Phân hệ Nghiệp vụ Chính (Core Modules)
1. **User / Auth Module:** Quản lý thông tin sinh viên KTX (UID, họ tên, SĐT, số phòng KTX, vai trò).
2. **Store Module:** Danh sách quán ăn liên kết KTX, thông tin chi tiết, trạng thái mở cửa, đánh giá trung bình.
3. **Menu Item Module:** Thực đơn theo từng quán, phân loại món, danh sách tùy biến (Size, Topping), trạng thái còn/hết hàng.
4. **Order & OrderItem Module (Trọng tâm):**
   - Tạo đơn hàng gom theo ca (`LUNCH_11H30`, `DINNER_18H00`, ...).
   - Price Snapshot: Lưu giá món ăn tại thời điểm đặt, không tính lại theo thực đơn hiện hành.
   - Tính toán subtotal, phí gom đơn, giảm giá, tổng tiền cuối cùng.
   - Vị trí nhận hàng chuẩn hóa tại KTX (ví dụ: `KTX Khu B – Trụ A1–A2 (Cổng B2)`).
   - Mã QR nhận hàng (`pickupQrCode`) và mã PIN 4 số cuối SĐT đối soát thủ công.
   - State machine trạng thái đơn nghiêm ngặt: `PENDING` $\rightarrow$ `CONFIRMED` $\rightarrow$ `PREPARING` $\rightarrow$ `READY` $\rightarrow$ `DELIVERING` $\rightarrow$ `DELIVERED` (hoặc `CANCELLED`).
5. **Payment Module:**
   - Abstraction qua `PaymentGateway` (`VietQrPaymentGateway`, `CodPaymentGateway`).
   - Sinh chuỗi nội dung chuyển khoản VietQR chuẩn hoá: `ORD_<ORDER_ID>`.
   - Hỗ trợ fallback chuyển đổi sang COD khi giao dịch thanh toán trực tuyến gặp lỗi.
6. **Dispute & Refund Module:**
   - Xử lý khiếu nại giao sai/hỏng/thiếu món sau khi nhận hàng.
   - Ràng buộc bằng chứng ảnh (`evidenceImageUrl`), tính toán hạn mức tiền hoàn hợp lệ.
   - Trạng thái khiếu nại: `PENDING` $\rightarrow$ `APPROVED` / `REJECTED` $\rightarrow$ `REFUNDED`.
7. **Review Module:** Đánh giá chất lượng món ăn của quán và shipper sau khi đơn hàng `DELIVERED`.
8. **Notification Module:** Lưu vết và điều phối thông báo trạng thái đơn hàng (chuẩn bị kết nối FCM).

---

## 5. Quy ước API Contract Thống nhất
- **Base URI:** `/api/v1`
- **Response Chuẩn:**
  ```json
  {
    "success": true,
    "message": "Operation successful",
    "data": { ... },
    "timestamp": "2026-11-02T08:30:00Z"
  }
  ```
- **Error Response Chuẩn:**
  ```json
  {
    "timestamp": "2026-11-02T08:30:00Z",
    "status": 400,
    "errorCode": "ORDER_EMPTY",
    "message": "Đơn hàng phải có ít nhất một món ăn.",
    "path": "/api/v1/orders",
    "errors": []
  }
  ```

---

## 6. Trạng thái Triển khai Hiện tại (Baseline Status)
| Hạng mục | Trạng thái | Ghi chú |
|---|:---:|---|
| Khởi tạo Project Spring Boot | **ĐÃ XONG** | Java 21, Spring Boot 4.1.2, Maven build thành công |
| Cấu trúc Package Layered | **ĐÃ XONG** | controller, service, repository, entity, dto, mapper, exception |
| **FLOW 0: Backend Foundation** | **HOÀN THÀNH** | Cấu hình MySQL `food_ordering_db`, Jakarta Validation, ApiResponse, ApiErrorResponse, GlobalExceptionHandler, SecurityConfig, CurrentUserProvider, Swagger |
| **FLOW 1: User / Authentication** | **HOÀN THÀNH** | `UserEntity`, `UserRepository`, `UserService`, `AuthController`, `UserController`, đồng bộ Firebase UID & hồ sơ KTX |
| **FLOW 2: Store + Menu** | **HOÀN THÀNH** | `StoreEntity`, `MenuItemEntity`, `StoreRepository`, `MenuItemRepository`, `StoreService`, `MenuItemService`, `DataInitializer` nạp dữ liệu mẫu KTX |
| **FLOW 3 & 4: Cart + Checkout + Order Creation** | **HOÀN THÀNH** | `OrderEntity`, `OrderItemEntity`, `OrderRepository`, `OrderItemRepository`, `OrderService`, `OrderController`, Price Snapshot, gom đơn KTX, mã QR/PIN, 39/39 test pass |
| **FLOW 5: Payment (VietQR & COD Fallback)** | **HOÀN THÀNH** | `PaymentEntity`, `PaymentRepository`, `VietQrPaymentGateway`, `PaymentService`, `PaymentController`, Napas 247 VietQR QuickLink, Fallback sang COD, 48/48 test pass |
| **FLOW 6: Order Pickup & Fulfillment** | **HOÀN THÀNH** | API-010 cập nhật trạng thái đơn, API-011 xác nhận nhận hàng bằng QR / PIN chuyển sang DELIVERED, ghi nhận deliveredAt, 55/55 test pass |
| **FLOW 7: Dispute & Refund** | **HOÀN THÀNH** | `DisputeEntity`, `DisputeRepository`, `DisputeService`, `DisputeController`, bắt buộc ảnh bằng chứng, thẩm định hoàn tiền <= finalAmount, 66/66 test pass |
| **FLOW 8: Review & Rating** | **HOÀN THÀNH** | `ReviewEntity`, `ReviewRepository`, `ReviewService`, `ReviewController`, đánh giá 1-5 sao quán/shipper, tự động tính lại ratingAvg & ratingCount, 73/73 test pass |
| **FLOW 9: Notification Center** | **HOÀN THÀNH** | `NotificationEntity`, `NotificationRepository`, `NotificationService`, `NotificationController`, quản lý thông báo, đánh dấu đã đọc, đếm chưa đọc, chuẩn bị FCM, 84/84 test pass |
| Database Schemas / Entities | **9 BẢNG ACTIVE** | Đã tạo và migrate `users`, `stores`, `menu_items`, `orders`, `order_items`, `payments`, `disputes`, `reviews`, `notifications` trên MySQL |
| Business Logic & Services | **TOÀN BỘ 10 FLOW HOÀN TẤT** | Toàn bộ các phân hệ nghiệp vụ chính từ Flow 0 đến Flow 9 đã được hiện thực hóa |
| REST Endpoints & Controllers | **ACTIVE** | Health, Auth, User, Store, Menu, Order, Payment, Dispute, Review, Notification APIs; 84/84 test pass |
| Bộ Tài liệu Context Chuẩn | **ĐÃ HOÀN THÀNH** | Đầy đủ 8 tệp tài liệu trong thư mục `docs/` |

---

## 7. Danh mục Tài liệu Tham chiếu (Documentation Map)
1. **Kiến trúc & Phân lớp:** [docs/ARCHITECTURE.md](./ARCHITECTURE.md)
2. **Đặc tả Cơ sở dữ liệu MySQL:** [docs/DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md)
3. **Danh mục API RESTful Toàn diện:** [docs/API_CATALOG.md](./API_CATALOG.md)
4. **Quy tắc Nghiệp vụ (Business Rules):** [docs/BUSINESS_RULES.md](./BUSINESS_RULES.md)
5. **Kế hoạch Chuyển đổi Firebase Firestore:** [docs/FIREBASE_MIGRATION.md](./FIREBASE_MIGRATION.md)
6. **Ma trận Theo vết Yêu cầu Lab 3:** [docs/REQUIREMENT_TRACEABILITY.md](./REQUIREMENT_TRACEABILITY.md)
7. **Nhật ký Thay đổi Dự án:** [docs/CHANGELOG.md](./CHANGELOG.md)
8. **Tài liệu Flutter Frontend:** `prm323-lab3-SE192947/docs/architecture.md` & `design-traceability.md`
