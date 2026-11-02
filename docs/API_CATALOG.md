# DANH MỤC API CHI TIẾT (API CATALOG SPECIFICATION)

- **Dự án:** Food Ordering App (PRM393 - Lab 3)
- **Base URL:** `/api/v1`
- **Quy tắc bắt buộc:** Mọi thay đổi về Endpoint, Method, Header, Request, Response, Status Code, Role hoặc Validation đều PHẢI cập nhật file này trong cùng task làm việc.

---

## 1. Bảng Tóm tắt Danh mục API (API Summary Table)

| API ID | Method | Endpoint | Mục đích (Purpose) | Auth | Vai trò (Role) | Luồng (Flow) | Màn hình (Screen) | Yêu cầu Lab 3 |
|:---:|:---:|---|---|:---:|:---:|:---:|:---:|:---:|
| **API-000** | `GET` | `/api/v1/health` | Kiểm tra trạng thái máy chủ (Health Check) | None | PUBLIC | Flow 0 | System | Baseline Foundation |
| **API-000b**| `POST`| `/api/v1/health/test-validation` | Kiểm thử cơ chế Validation & ApiErrorResponse | None | PUBLIC | Flow 0 | System | Baseline Foundation |
| **API-001** | `POST` | `/api/v1/auth/sync-user` | Đồng bộ/Đăng ký user từ Google Auth / SĐT | Bearer | ALL | Flow 1 | S0, S0_Reg | Auth Sync |
| **API-002** | `GET` | `/api/v1/users/me` | Lấy thông tin tài khoản hiện tại | Bearer | ALL | Flow 1 | S13 | Profile Info |
| **API-002b**| `PATCH`| `/api/v1/users/me` | Cập nhật hồ sơ sinh viên KTX | Bearer | ALL | Flow 1 | S13 | Profile Update |
| **API-002c**| `GET` | `/api/v1/users/{id}` | Lấy thông tin người dùng theo ID | Bearer | ALL | Flow 1 | System | User Detail |
| **API-003** | `GET` | `/api/v1/stores` | Lấy danh sách quán ăn theo KTX / Từ khóa | None | PUBLIC | Flow 2 | S1 | Store Search & Filter |
| **API-003b**| `POST`| `/api/v1/stores` | Thêm mới quán ăn KTX | Bearer | STORE/ADMIN | Flow 2 | System | Store Creation |
| **API-004** | `GET` | `/api/v1/stores/{storeId}` | Lấy chi tiết thông tin quán ăn | None | PUBLIC | Flow 2 | S2 | Store Details |
| **API-004b**| `PUT` | `/api/v1/stores/{storeId}` | Cập nhật thông tin quán ăn | Bearer | STORE/ADMIN | Flow 2 | System | Store Update |
| **API-005** | `GET` | `/api/v1/stores/{storeId}/menu-items` | Lấy danh sách thực đơn của quán | None | PUBLIC | Flow 2 | S2, S3 | Menu & Customization |
| **API-005b**| `GET` | `/api/v1/menu-items/{id}` | Lấy chi tiết món ăn & tùy chọn Size/Topping | None | PUBLIC | Flow 2 | S3 | Food Item Detail |
| **API-005c**| `POST`| `/api/v1/stores/{storeId}/menu-items` | Thêm món ăn mới vào thực đơn | Bearer | STORE/ADMIN | Flow 2 | System | Menu Item Creation |
| **API-005d**| `PUT` | `/api/v1/menu-items/{id}` | Cập nhật thông tin món ăn (giá, hết hàng) | Bearer | STORE/ADMIN | Flow 2 | System | Menu Item Update |
| **API-006** | `POST` | `/api/v1/orders` | Tạo đơn hàng mới (Gom đơn ca KTX) | Bearer | STUDENT | Flow 1 | S4 | Order Creation |
| **API-007** | `GET` | `/api/v1/orders/{orderId}` | Lấy chi tiết đơn hàng (Theo dõi tiến trình) | Bearer | OWNER/ADMIN | Flow 1, 2 | S6, S7 | Order Tracking & QR |
| **API-008** | `GET` | `/api/v1/orders` | Danh sách lịch sử đơn hàng của người dùng | Bearer | STUDENT | Flow 3 | S10 | Order History |
| **API-009** | `POST` | `/api/v1/orders/{orderId}/cancel` | Hủy đơn hàng trước khi quán chế biến | Bearer | OWNER | Flow 1 | S6 | Order Cancellation |
| **API-010** | `PATCH`| `/api/v1/orders/{orderId}/status` | Chuyển trạng thái đơn hàng (State machine) | Bearer | STORE/SHIPPER | Flow 1, 2 | Admin/Store | Order State Transition |
| **API-011** | `POST` | `/api/v1/orders/{orderId}/confirm-pickup` | Xác nhận đã nhận hàng bằng QR / PIN | Bearer | STUDENT/SHIPPER| Flow 2 | S7 | Delivery Confirmation |
| **API-012** | `POST` | `/api/v1/payments/{orderId}/generate-qr` | Sinh link và mã VietQR thanh toán | Bearer | STUDENT | Flow 1 | S5 | VietQR Generation |
| **API-013** | `POST` | `/api/v1/payments/{orderId}/switch-to-cod` | Đổi phương thức thanh toán sang Tiền mặt | Bearer | STUDENT | Flow 1 Alt | S5 -> S4 | Payment COD Fallback |
| **API-014** | `POST` | `/api/v1/disputes` | Gửi khiếu nại món hỏng/thiếu kèm ảnh | Bearer | STUDENT | Flow 3 | S8 | Dispute Submission |
| **API-015** | `GET` | `/api/v1/disputes/{disputeId}` | Xem chi tiết tiến trình khiếu nại & hoàn tiền | Bearer | OWNER/ADMIN | Flow 3 | S8b, S8c | Dispute Tracking |
| **API-016** | `GET` | `/api/v1/disputes` | Danh sách lịch sử khiếu nại của sinh viên | Bearer | STUDENT | Flow 3 | S8b | Dispute History |
| **API-017** | `POST` | `/api/v1/reviews` | Đánh giá chất lượng quán ăn và shipper | Bearer | STUDENT | Flow 2 | S9 | Rating & Feedback |
| **API-018** | `GET` | `/api/v1/stores/{storeId}/reviews` | Lấy danh sách đánh giá của quán | None | PUBLIC | Flow 1 | S2 | Store Reviews |
| **API-019** | `GET` | `/api/v1/notifications` | Lấy danh sách thông báo đơn hàng | Bearer | STUDENT | Flow 2 | S12 | Notification Center |

---

## 2. Đặc tả Chi tiết Từng Endpoint (Detailed Endpoint Specifications)

### API-001: Đồng bộ / Đăng ký Người dùng
- **API ID:** API-001
- **HTTP Method:** `POST`
- **Endpoint:** `/api/v1/auth/sync-user`
- **Purpose:** Tiếp nhận thông tin định danh sau khi đăng nhập Google hoặc OTP thành công từ Flutter client, lưu hoặc cập nhật bản ghi trong CSDL.
- **Feature / Flow:** Flow 0 (Authentication)
- **Auth:** Bearer Token (hoặc Firebase ID Token)
- **Role:** ALL
- **Request Body:**
  ```json
  {
    "firebaseUid": "google_1234567890",
    "email": "student@fpt.edu.vn",
    "displayName": "Nguyễn Văn A",
    "phone": "0987654321",
    "avatarUrl": "https://lh3.googleusercontent.com/...",
    "ktxBuilding": "Khu B - Tòa BA1",
    "ktxRoom": "312"
  }
  ```
- **Response Body (200 OK):**
  ```json
  {
    "success": true,
    "message": "User synchronized successfully",
    "data": {
      "id": 1,
      "firebaseUid": "google_1234567890",
      "email": "student@fpt.edu.vn",
      "displayName": "Nguyễn Văn A",
      "phone": "0987654321",
      "role": "STUDENT",
      "ktxBuilding": "Khu B - Tòa BA1",
      "ktxRoom": "312"
    }
  }
  ```
- **Error Codes:** `INVALID_EMAIL` (400), `USER_SYNC_FAILED` (500)
- **Related Screen:** S0, S0_Reg

---

### API-003: Danh sách Quán ăn (Tra cứu & Lọc)
- **API ID:** API-003
- **HTTP Method:** `GET`
- **Endpoint:** `/api/v1/stores`
- **Purpose:** Tìm kiếm quán ăn theo tên món, tên quán và lọc theo phân khu KTX.
- **Feature / Flow:** Flow 1 (Home & Search)
- **Auth:** None (Public)
- **Query Parameters:**
  - `keyword` (string, optional): Từ khóa tìm kiếm (vd: "Cơm tấm")
  - `campusZone` (string, optional): Khu KTX (`KTX_KHU_A`, `KTX_KHU_B`)
  - `page` (int, default 0), `size` (int, default 10)
- **Response Body (200 OK):**
  ```json
  {
    "success": true,
    "message": "Stores retrieved successfully",
    "data": {
      "content": [
        {
          "id": 1,
          "name": "Cơm Tấm Sườn Bì KTX B2",
          "description": "Cơm tấm đêm sinh viên, phục vụ tận trụ KTX",
          "address": "Cổng B2 KTX ĐHQG",
          "campusZone": "KTX_KHU_B",
          "avatarUrl": "https://img.food.vn/comtam.jpg",
          "isActive": true,
          "ratingAvg": 4.8,
          "ratingCount": 128
        }
      ],
      "pageNumber": 0,
      "pageSize": 10,
      "totalElements": 1,
      "totalPages": 1
    }
  }
  ```
- **Error Cases:** Khi không có dữ liệu trả về mảng rỗng `[]` (hỗ trợ Empty State C08 tại S1).
- **Related Screen:** S1

---

### API-005: Danh sách Thực đơn Quán ăn
- **API ID:** API-005
- **HTTP Method:** `GET`
- **Endpoint:** `/api/v1/stores/{storeId}/menu-items`
- **Purpose:** Nạp danh sách món ăn, nhóm theo danh mục và cấu hình tùy biến Size/Topping.
- **Feature / Flow:** Flow 1 (Store Detail & Food Customization)
- **Path Parameters:** `storeId` (Long, required)
- **Query Parameters:** `category` (string, optional)
- **Response Body (200 OK):**
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 101,
        "storeId": 1,
        "name": "Cơm Sườn Nướng Chả Trứng",
        "description": "Sườn ướp mật ong, kèm canh rong biển",
        "imageUrl": "https://img.food.vn/suon.jpg",
        "basePrice": 35000.00,
        "category": "COM",
        "isAvailable": true,
        "customizationOptions": [
          {
            "group": "Kích cỡ",
            "required": true,
            "options": [
              { "name": "Vừa", "extraPrice": 0.00 },
              { "name": "Lớn (+Cơm)", "extraPrice": 5000.00 }
            ]
          },
          {
            "group": "Topping thêm",
            "required": false,
            "options": [
              { "name": "Trứng ốp la", "extraPrice": 6000.00 },
              { "name": "Chả bì", "extraPrice": 8000.00 }
            ]
          }
        ]
      }
    ]
  }
  ```
- **Related Screen:** S2, S3

---

### API-006: Tạo Đơn hàng Gom đơn KTX
- **API ID:** API-006
- **HTTP Method:** `POST`
- **Endpoint:** `/api/v1/orders`
- **Purpose:** Xác nhận đặt giỏ hàng, áp dụng ca gom đơn KTX để chia ship, sinh snapshot giá và mã đối soát QR/PIN.
- **Feature / Flow:** Flow 1 (Cart Checkout)
- **Auth:** Bearer Token
- **Role:** STUDENT
- **Request Body:**
  ```json
  {
    "storeId": 1,
    "slotTime": "CA_TRUA_11H30",
    "pickupLocation": "KTX Khu B – Trụ A1–A2 (Cổng B2)",
    "paymentMethod": "VIETQR",
    "note": "Xin thêm ớt và nước mắm ngọt",
    "items": [
      {
        "menuItemId": 101,
        "quantity": 2,
        "selectedOptions": [
          { "group": "Kích cỡ", "choice": "Lớn (+Cơm)", "extraPrice": 5000.00 },
          { "group": "Topping thêm", "choice": "Trứng ốp la", "extraPrice": 6000.00 }
        ]
      }
    ]
  }
  ```
- **Validation Rules:**
  - `storeId` @NotNull
  - `items` @NotEmpty (Tối thiểu 1 món)
  - `quantity` @Min(1)
  - `slotTime` @NotBlank
  - `paymentMethod` must be in `VIETQR`, `COD`
- **Response Body (201 Created):**
  ```json
  {
    "success": true,
    "message": "Order created successfully",
    "data": {
      "id": 501,
      "orderCode": "KTX-20261102-0501",
      "subtotalAmount": 92000.00,
      "deliveryFee": 5000.00,
      "groupDiscountAmount": 3000.00,
      "finalAmount": 94000.00,
      "status": "PENDING",
      "paymentMethod": "VIETQR",
      "paymentStatus": "UNPAID",
      "pickupLocation": "KTX Khu B – Trụ A1–A2 (Cổng B2)",
      "pickupQrCode": "QR_HASH_a8f9c1e2d4b5",
      "pickupPin": "4321",
      "createdAt": "2026-11-02T10:15:00Z"
    }
  }
  ```
- **Error Codes:** `ORDER_EMPTY` (400), `STORE_NOT_FOUND` (404), `ITEM_NOT_AVAILABLE` (422)
- **Related Screen:** S4

---

### API-007: Chi tiết Đơn hàng & Theo dõi (Live Tracking)
- **API ID:** API-007
- **HTTP Method:** `GET`
- **Endpoint:** `/api/v1/orders/{orderId}`
- **Purpose:** Lấy đầy đủ thông tin trạng thái đơn hàng phục vụ màn hình S6 và xuất mã QR nhận hàng tại S7.
- **Feature / Flow:** Flow 1, Flow 2
- **Path Parameters:** `orderId` (Long, required)
- **Auth:** Bearer Token (Chỉ chính chủ đơn hoặc Quán/Shipper phụ trách)
- **Response Body (200 OK):** Chứa thông tin đơn hàng, danh sách order items (price snapshot), trạng thái chuyển giao và mã QR đối soát.
- **Related Screen:** S6, S7

---

### API-008: Lịch sử Đơn hàng Sinh viên
- **API ID:** API-008
- **HTTP Method:** `GET`
- **Endpoint:** `/api/v1/orders`
- **Purpose:** Lấy toàn bộ danh sách đơn hàng đã đặt của sinh viên hiện tại, sắp xếp theo thời gian mới nhất.
- **Feature / Flow:** Flow 1 (Order History)
- **Auth:** Bearer Token
- **Response Body (200 OK):** Danh sách các đơn hàng của sinh viên kèm danh sách món ăn snapshot.
- **Related Screen:** S6, S8

---

### API-009: Hủy Đơn hàng Đang Chờ (Cancel Order)
- **API ID:** API-009
- **HTTP Method:** `POST`
- **Endpoint:** `/api/v1/orders/{orderId}/cancel`
- **Purpose:** Sinh viên hủy đơn hàng khi quán chưa chuẩn bị món ăn (chỉ áp dụng khi trạng thái là `PENDING`).
- **Feature / Flow:** Flow 1 (Order Management)
- **Path Parameters:** `orderId` (Long, required)
- **Auth:** Bearer Token
- **Response Body (200 OK):** Thông tin đơn hàng sau khi cập nhật trạng thái `CANCELLED`.
- **Error Codes:** `ORDER_CANNOT_BE_CANCELLED` (400), `ORDER_NOT_FOUND` (404)
- **Related Screen:** S6

---

### API-012: Sinh link và mã VietQR Thanh toán
- **API ID:** API-012
- **HTTP Method:** `POST`
- **Endpoint:** `/api/v1/payments/{orderId}/generate-qr`
- **Purpose:** Khởi tạo mã thanh toán VietQR chuẩn Napas 247 QuickLink, gắn nội dung chuyển khoản bắt buộc `ORD_<ORDER_ID>`.
- **Feature / Flow:** Flow 1 (Payment)
- **Path Parameters:** `orderId` (Long, required)
- **Auth:** Bearer Token
- **Response Body (200 OK):**
  ```json
  {
    "success": true,
    "message": "Sinh mã VietQR thanh toán thành công",
    "data": {
      "id": 10,
      "orderId": 501,
      "orderCode": "KTX-20261102-0501",
      "paymentMethod": "VIETQR",
      "amount": 85000.00,
      "status": "UNPAID",
      "vietqrContent": "ORD_501",
      "qrUrl": "https://img.vietqr.io/image/970422-0987654321-compact2.png?amount=85000&addInfo=ORD_501&accountName=KTX+FOOD+ORDERING",
      "bankBin": "970422",
      "bankName": "MBBank",
      "accountNumber": "0987654321",
      "accountName": "KTX FOOD ORDERING"
    }
  }
  ```
- **Error Codes:** `ORDER_NOT_FOUND` (404), `BUSINESS_RULE_VIOLATION` (422)
- **Related Screen:** S5

---

### API-013: Chuyển đổi Phương thức Thanh toán sang Tiền mặt (COD Fallback)
- **API ID:** API-013
- **HTTP Method:** `POST`
- **Endpoint:** `/api/v1/payments/{orderId}/switch-to-cod`
- **Purpose:** Kích hoạt cơ chế dự phòng khi app ngân hàng lỗi hoặc sinh viên chọn trả tiền mặt khi nhận hàng tại cổng KTX.
- **Feature / Flow:** Flow 1 Alt (Payment COD Fallback)
- **Path Parameters:** `orderId` (Long, required)
- **Auth:** Bearer Token
- **Response Body (200 OK):**
  ```json
  {
    "success": true,
    "message": "Đã chuyển đổi phương thức thanh toán sang Tiền mặt (COD)",
    "data": {
      "id": 10,
      "orderId": 501,
      "orderCode": "KTX-20261102-0501",
      "paymentMethod": "COD",
      "amount": 85000.00,
      "status": "UNPAID"
    }
  }
  ```
- **Business Rule:** Phương thức thanh toán chuyển sang `COD`, trạng thái thanh toán là `UNPAID`, đơn hàng chuyển sang `CONFIRMED`.
- **Error Codes:** `ORDER_NOT_FOUND` (404), `INVALID_ORDER_STATUS` (422)
- **Related Screen:** S5 -> S4

---

### API-010: Cập nhật Trạng thái Đơn hàng (State Machine Transition)
- **API ID:** API-010
- **HTTP Method:** `PATCH`
- **Endpoint:** `/api/v1/orders/{orderId}/status`
- **Purpose:** Quán ăn và Shipper cập nhật trạng thái đơn theo tiến trình nấu món và giao nhận KTX (`PREPARING`, `READY`, `DELIVERING`).
- **Feature / Flow:** Flow 1, Flow 2
- **Path Parameters:** `orderId` (Long, required)
- **Auth:** Bearer Token
- **Request Body:**
  ```json
  {
    "status": "PREPARING"
  }
  ```
- **Response Body (200 OK):** Thông tin đơn hàng cập nhật trạng thái mới.
- **Error Codes:** `ORDER_NOT_FOUND` (404), `INVALID_ORDER_STATUS` (422)
- **Related Screen:** S6

---

### API-011: Xác nhận Đã Nhận Hàng (Pickup Confirmation)
- **API ID:** API-011
- **HTTP Method:** `POST`
- **Endpoint:** `/api/v1/orders/{orderId}/confirm-pickup`
- **Purpose:** Sinh viên xuất trình QR / PIN tại trụ KTX, shipper hoặc sinh viên bấm xác nhận giao thành công, chuyển trạng thái đơn sang `DELIVERED`.
- **Feature / Flow:** Flow 2 (Pickup at Gate)
- **Request Body:**
  ```json
  {
    "verificationMethod": "QR_SCAN",
    "code": "QR_HASH_a8f9c1e2d4b5"
  }
  ```
  *(Hoặc `verificationMethod`: `PIN`, `code`: `4321` khi quét QR không thành công)*
- **Response Body (200 OK):**
  ```json
  {
    "success": true,
    "message": "Order delivered successfully",
    "data": {
      "orderId": 501,
      "status": "DELIVERED",
      "deliveredAt": "2026-11-02T11:45:00Z"
    }
  }
  ```
- **Validation:** Đơn hàng phải đang ở trạng thái `DELIVERING`. Mã QR hoặc PIN phải khớp với đơn hàng.
- **Error Codes:** `INVALID_PICKUP_CODE` (400), `INVALID_ORDER_STATUS` (422)
- **Related Screen:** S7

---

### API-014: Gửi Khiếu nại & Yêu cầu Hoàn tiền (Dispute)
- **API ID:** API-014
- **HTTP Method:** `POST`
- **Endpoint:** `/api/v1/disputes`
- **Purpose:** Báo cáo sự cố đồ ăn (hỏng, đổ vỡ, thiếu món), bắt buộc đính kèm URL ảnh bằng chứng từ Firebase Storage, tự động thẩm định số tiền hoàn.
- **Feature / Flow:** Flow 3 (Dispute & Refund)
- **Auth:** Bearer Token
- **Role:** STUDENT
- **Request Body:**
  ```json
  {
    "orderId": 501,
    "reason": "DAMAGED_FOOD",
    "description": "Hộp cơm bị vỡ nắp, canh đổ ra túi nilon khi nhận tại trụ B2.",
    "evidenceImages": [
      "https://firebasestorage.googleapis.com/v0/b/.../reports%2Fuid123%2Fevidence1.jpg"
    ],
    "refundRequestedAmount": 46000.00
  }
  ```
- **Validation Rules:**
  - `orderId` @NotNull (Đơn phải ở trạng thái `DELIVERED`)
  - `evidenceImages` @NotEmpty (Bắt buộc phải có ít nhất 1 ảnh bằng chứng, không cho phép gửi khi rỗng)
  - `refundRequestedAmount` @Positive (Không được vượt quá `finalAmount` của đơn hàng)
- **Response Body (201 Created):**
  ```json
  {
    "success": true,
    "message": "Dispute submitted successfully",
    "data": {
      "id": 901,
      "orderId": 501,
      "reason": "DAMAGED_FOOD",
      "status": "PENDING",
      "refundRequestedAmount": 46000.00,
      "createdAt": "2026-11-02T12:00:00Z"
    }
  }
  ```
- **Error Codes:** `DISPUTE_EVIDENCE_REQUIRED` (400), `DISPUTE_ALREADY_EXISTS` (409), `REFUND_EXCEEDS_ORDER_TOTAL` (422)
- **Related Screen:** S8, S8b

---

### API-015: Chi tiết Tiến trình Khiếu nại
- **API ID:** API-015
- **HTTP Method:** `GET`
- **Endpoint:** `/api/v1/disputes/{id}`
- **Purpose:** Xem chi tiết tiến trình khiếu nại, ảnh bằng chứng và kết quả thẩm định hoàn tiền.
- **Feature / Flow:** Flow 3 (Dispute Detail)
- **Path Parameters:** `id` (Long, required)
- **Auth:** Bearer Token
- **Response Body (200 OK):** Thông tin chi tiết hồ sơ khiếu nại.
- **Related Screen:** S8b, S8c

---

### API-016: Lịch sử Khiếu nại của Sinh viên
- **API ID:** API-016
- **HTTP Method:** `GET`
- **Endpoint:** `/api/v1/disputes`
- **Purpose:** Lấy toàn bộ danh sách khiếu nại đã gửi của người dùng hiện tại.
- **Feature / Flow:** Flow 3 (Dispute History)
- **Auth:** Bearer Token
- **Response Body (200 OK):** Mảng các hồ sơ khiếu nại của sinh viên.
- **Related Screen:** S8b

---

### API-017: Đánh giá Quán & Shipper (Review)
- **API ID:** API-017
- **HTTP Method:** `POST`
- **Endpoint:** `/api/v1/reviews`
- **Purpose:** Thu thập số sao và đánh giá trải nghiệm món ăn cùng shipper sau khi đơn hàng giao thành công.
- **Feature / Flow:** Flow 2 (Review)
- **Request Body:**
  ```json
  {
    "orderId": 501,
    "storeRating": 5,
    "shipperRating": 5,
    "comment": "Đồ ăn còn nóng hổi, shipper giao đúng giờ tại trụ A1."
  }
  ```
- **Validation:** `storeRating` và `shipperRating` từ 1 đến 5 sao. Đơn hàng phải `DELIVERED` và chưa từng được đánh giá.
- **Error Codes:** `ORDER_NOT_DELIVERED` (400), `REVIEW_ALREADY_EXISTS` (409)
- **Related Screen:** S9

---

### API-018: Danh sách Đánh giá của Quán ăn
- **API ID:** API-018
- **HTTP Method:** `GET`
- **Endpoint:** `/api/v1/stores/{storeId}/reviews`
- **Purpose:** Xem danh sách nhận xét, đánh giá của sinh viên về quán ăn (hỗ trợ phân trang `page`, `size`).
- **Feature / Flow:** Flow 2 (Store Reviews)
- **Path Parameters:** `storeId` (Long, required)
- **Query Parameters:** `page` (default 0), `size` (default 10)
- **Response Body (200 OK):** `PageResponse<ReviewResponse>` chứa danh sách đánh giá, điểm sao và thông tin người đánh giá.
- **Related Screen:** S1, S2

---

### API-018b: Xem Đánh giá của Đơn hàng
- **API ID:** API-018b
- **HTTP Method:** `GET`
- **Endpoint:** `/api/v1/orders/{orderId}/review`
- **Purpose:** Lấy thông tin đánh giá đã gửi cho đơn hàng cụ thể.
- **Feature / Flow:** Flow 2 (Order Review)
- **Path Parameters:** `orderId` (Long, required)
- **Response Body (200 OK):** `ReviewResponse` chi tiết đánh giá đơn hàng.
- **Related Screen:** S6, S9

