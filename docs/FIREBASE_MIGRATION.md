# KẾ HOẠCH & HƯỚNG DẪN CHUYỂN ĐỔI SANG FIREBASE (FIREBASE MIGRATION PLAN)

- **Dự án:** Food Ordering App (PRM393 - Lab 3)
- **Mục tiêu:** Chuyển đổi Data Source từ RDBMS (MySQL 8.x / JPA) sang NoSQL (Google Cloud Firestore / Firebase Ecosystem) mà không làm biến động hay viết lại tầng Business Logic.

---

## 1. Ánh xạ Bảng MySQL sang Firestore Collections (Collection Mapping)

| MySQL Table | Firestore Architecture | Collection Path | Cấu trúc Lưu trữ (Document Schema) |
|---|---|---|---|
| `users` | Top-level Collection | `/users/{userId}` | Key: `userId` (dùng luôn `firebase_uid`). Chứa hồ sơ, SĐT, KTX building/room. |
| `stores` | Top-level Collection | `/stores/{storeId}` | Document chứa thông tin quán, giờ mở cửa, rating. |
| `menu_items` | Subcollection của Store | `/stores/{storeId}/menus/{menuItemId}` | Subcollection giúp query menu theo quán tự nhiên, không cần join. |
| `orders` | Top-level Collection | `/orders/{orderId}` | Document chứa toàn bộ thông tin đơn hàng, embedded array `items` (Price Snapshot). |
| `order_items` | Embedded Array trong Order | Thuộc tính `items: [...]` trong `/orders/{orderId}` | Không cần collection riêng vì Order Items luôn đọc/ghi cùng Order và bất biến. |
| `payments` | Embedded Map hoặc Subcollection | Thuộc tính `payment: {...}` trong `/orders/{orderId}` | Thông tin VietQR/COD gắn trực tiếp trong Order document. |
| `disputes` | Top-level Collection | `/disputes/{disputeId}` | Lưu khiếu nại kèm mảng URL ảnh Cloud Storage `evidenceImages: [...]`. |
| `reviews` | Top-level Collection | `/reviews/{reviewId}` | Đánh giá sao của quán và shipper; đồng thời cập nhật aggregate rating trên store document. |
| `notifications` | Subcollection của User | `/users/{userId}/notifications/{notiId}` | Hỗ trợ lắng nghe thông báo cá nhân hóa theo từng người dùng. |

---

## 2. Chi tiết Ánh xạ Trường Dữ liệu (Field-by-Field Mapping)

### 2.1. Quán ăn & Món ăn (`stores` & `menu_items`)
- **MySQL `stores` $\rightarrow$ Firestore `/stores/{storeId}`:**
  - `id` (BIGINT) $\rightarrow$ Document ID (`storeId` - String/Auto-ID)
  - `name` $\rightarrow$ `name` (String)
  - `campus_zone` $\rightarrow$ `campusZone` (String)
  - `rating_avg`, `rating_count` $\rightarrow$ `ratingAvg` (Number), `ratingCount` (Number)
- **MySQL `menu_items` $\rightarrow$ Firestore `/stores/{storeId}/menus/{menuItemId}`:**
  - `base_price` $\rightarrow$ `basePrice` (Number)
  - `customization_options` (JSON) $\rightarrow$ `customizationOptions` (Array of Maps)
  - `is_available` $\rightarrow$ `isAvailable` (Boolean)

### 2.2. Đơn hàng (`orders` & `order_items`)
- **MySQL `orders` $\rightarrow$ Firestore `/orders/{orderId}`:**
  - `order_code` $\rightarrow$ `orderCode` (String)
  - `user_id` $\rightarrow$ `userId` (String reference)
  - `store_id` $\rightarrow$ `storeId` (String reference)
  - `status` $\rightarrow$ `status` (String: `PENDING`, `CONFIRMED`, `PREPARING`, `READY`, `DELIVERING`, `DELIVERED`, `CANCELLED`)
  - `final_amount` $\rightarrow$ `finalAmount` (Number)
  - `pickup_qr_code` $\rightarrow$ `pickupQrCode` (String)
  - `pickup_pin` $\rightarrow$ `pickupPin` (String)
  - **Embedded `items`:**
    ```json
    [
      {
        "menuItemId": "item_101",
        "itemName": "Cơm Sườn Nướng",
        "unitPrice": 35000,
        "quantity": 2,
        "selectedOptions": [{ "group": "Size", "choice": "Lớn", "extraPrice": 5000 }],
        "itemTotalAmount": 80000
      }
    ]
    ```

---

## 3. Phân tích Tác động Chuyển đổi (Impact Analysis)

### 3.1. Những thành phần KHÔNG THAY ĐỔI (100% Reusable):
1. **Toàn bộ Controllers (`*Controller.java`):** API Contract (Endpoints, Request DTO, Response DTO, HTTP Status) được giữ nguyên vẹn 100%. Mobile app không nhận thấy sự thay đổi dưới database.
2. **Toàn bộ Business Services (`*Service.java`):** Logic tính tiền, logic kiểm tra chuyển trạng thái đơn (State Machine), logic bắt buộc có ảnh khiếu nại... giữ nguyên vẹn 100% vì chỉ giao tiếp qua Repository Interface.
3. **DTOs & Exception Handling:** `ApiResponse`, `ApiErrorResponse`, `GlobalExceptionHandler` hoàn toàn độc lập với hệ quản trị CSDL.

### 3.2. Những thành phần CẦN THAY THẾ (Repositories):
| MySQL Implementation Hiện Tại | Firestore Implementation Tương Lai |
|---|---|
| `MySqlStoreRepository` | `FirestoreStoreRepository` |
| `MySqlOrderRepository` | `FirestoreOrderRepository` |
| `MySqlDisputeRepository` | `FirestoreDisputeRepository` |
| `MySqlReviewRepository` | `FirestoreReviewRepository` |
| `MySqlNotificationRepository` | `FirestoreNotificationRepository` |

---

## 4. Các Vấn đề Kỹ thuật Cần Xử lý khi Migration

### 4.1. Thay thế Khóa Tự tăng (Auto-increment ID)
- MySQL dùng `BIGINT AUTO_INCREMENT`.
- Firestore dùng chuỗi Hash ngẫu nhiên 20 ký tự (Auto-ID) hoặc mã sinh có quy tắc.
- **Giải pháp:** Thiết kế ID trong DTO và Domain Model dạng String hoặc tương thích đa hình (vd: ID string hỗ trợ cả `"123"` lẫn `"fs_ab12cd34"`). Mã đơn hàng nghiệp vụ (`orderCode`) duy trì định dạng `KTX-YYYYMMDD-XXXX`.

### 4.2. Không hỗ trợ SQL JOIN trong Firestore
- Trong MySQL: Truy vấn `Order` có thể JOIN `Store`, `User`, `OrderItem`.
- Trong Firestore:
  - `OrderItem` được nhúng trực tiếp (Denormalization) vào tài liệu `Order`.
  - Thông tin tối thiểu của Store (`storeName`, `storeAvatar`) có thể snapshot luôn vào Order để khi xem danh sách đơn hàng không cần gọi thêm truy vấn đọc Store.

### 4.3. Xác thực (Authentication Migration)
- **Hiện tại:** Giả lập qua `CurrentUserProvider` hoặc Bearer Token Header.
- **Khi chuyển sang Firebase:**
  - Tích hợp `firebase-admin` SDK trên Spring Boot.
  - Viết `FirebaseAuthFilter` chặn `Authorization: Bearer <FIREBASE_ID_TOKEN>`.
  - Gọi `FirebaseAuth.getInstance().verifyIdToken(token)` $\rightarrow$ Trích xuất `uid`, `email` $\rightarrow$ Nạp vào `CurrentUserProvider`.

### 4.4. Lưu trữ Tệp ảnh Khiếu nại (Cloud Storage Migration)
- Ảnh khiếu nại (S8) được tải trực tiếp từ Flutter client lên Cloud Storage tại đường dẫn: `reports/{uid}/{timestamp}_{filename}.jpg`.
- Frontend gửi URL tải về (Download URL) tới API `POST /api/v1/disputes`.
- Backend lưu danh sách URL này vào trường `evidenceImages`.

### 4.5. Realtime Order Tracking (Cập nhật Đơn hàng Thời gian thực)
- Trong Firestore: Flutter Client (`OrderTrackingViewModel`) có thể dùng `snapshots()` lắng nghe trực tiếp sự thay đổi trạng thái của `/orders/{orderId}` mà không cần polling liên tục vào Backend.
- Backend Spring Boot đóng vai trò cập nhật Document trên Firestore khi Shipper/Store chuyển trạng thái đơn.
