# TÀI LIỆU ĐẶC TẢ KỸ THUẬT & API BACKEND (PRM393 LAB 3)
## SPRING BOOT RESTFUL API - HỆ THỐNG GOM ĐƠN KTX ĐHQG

---

### 1. THÔNG TIN HỆ THỐNG MÁY CHỦ
* **Môi trường phát triển**: Spring Boot 4.1.2, Java 21, H2 Database (Persistent File Mode).
* **Môi trường Triển khai Production/VPS**:
  * **Public IP VPS**: `http://103.179.189.120:8080/api/v1`
  * **Android Emulator**: `http://10.0.2.2:8080/api/v1`
  * **Localhost / USB Debugging**: `http://127.0.0.1:8080/api/v1`
* **Swagger OpenAPI Documentation**: `http://<server-ip>:8080/swagger-ui.html`
* **H2 Web Console**: `http://<server-ip>:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/food_ordering_db`)

---

### 2. MA TRẬN PHÂN QUYỀN VAI TRÒ (MULTI-ROLE MATRIX)
Hệ thống xác thực người dùng thông qua Header `X-User-Id` và Bearer Token:
| ID | Email Mẫu | Họ và Tên | Vai trò (Role) | Chức năng chính |
|---|---|---|---|---|
| **1** | `student@fpt.edu.vn` | Ngô Hoàng Trường Đạt | `STUDENT` | Đặt món, gom đơn, nạp tiền ví, khiếu nại món hỏng |
| **2** | `coba.owner@fpt.edu.vn` | Cô Ba Cơm Tấm | `STORE_OWNER` | Quản lý món ăn, bật/tắt bán, xác nhận chuẩn bị món |
| **3** | `shipper.hung@fpt.edu.vn` | Nguyễn Hùng | `SHIPPER` | Nhận lô gom đơn tại quán, giao tới sảnh KTX, nhập PIN |
| **4** | `admin.ktx@fpt.edu.vn` | Ban Quản Lý KTX | `ADMIN` | Sử dụng Web Portal, xem ảnh bằng chứng, duyệt hoàn tiền |

---

### 3. DANH MỤC API NÂNG CẤP PHỤC VỤ LAB 3

#### 3.1. Nhóm API Kiểm tra Kết nối & Chẩn đoán Lỗi
* **GET `/api/v1/health/ping`**:
  * **Mô tả**: Endpoint phản hồi tức thì siêu nhẹ để ứng dụng Flutter Client trên điện thoại thật kiểm tra thông mạng tới VPS trước khi đăng nhập.
  * **Response**:
    ```json
    {
      "success": true,
      "message": "Ping thành công",
      "data": {
        "status": "UP",
        "message": "Pong! Máy chủ Backend KTX PRM393 Lab 3 đang hoạt động sẵn sàng",
        "timestamp": "2026-11-05T00:45:00Z",
        "environment": "Production-Ready",
        "version": "Lab 3 - v1.0"
      }
    }
    ```

* **POST `/api/v1/logs/client-crash`**:
  * **Mô tả**: Ghi nhận log chẩn đoán sự cố Crash từ thiết bị di động đồng thời với Firebase Crashlytics để lưu vết tại Backend.
  * **Request Body**:
    ```json
    {
      "exceptionMessage": "Fatal Crash: Simulated crashlytics test",
      "stackTrace": "CrashlyticsService.forceCrashTest (crashlytics_service.dart:36)...",
      "deviceModel": "Pixel 7 / Android 14",
      "appVersion": "1.0.0+1",
      "userId": "1"
    }
    ```

#### 3.2. Nhóm API Cổng Web Quản Trị KTX (Web Admin Portal)
* **GET `/api/v1/admin/dashboard-stats`**:
  * **Mô tả**: Cung cấp số liệu tổng quan KPI real-time cho giao diện Web Admin.
  * **Response**:
    ```json
    {
      "success": true,
      "data": {
        "totalRevenue": 28540000.0,
        "totalOrders": 642,
        "activeOrders": 18,
        "pendingDisputes": 3,
        "totalUsers": 1250,
        "disputeResolutionRate": 94.5
      }
    }
    ```

* **GET `/api/v1/admin/disputes`**:
  * **Mô tả**: Lấy toàn bộ danh sách khiếu nại kèm link ảnh bằng chứng món hỏng độ phân giải cao phục vụ phóng to thẩm định.

* **PATCH `/api/v1/disputes/{id}/resolve`**:
  * **Mô tả**: Admin ra phán quyết "Duyệt hoàn tiền" hoặc "Từ chối". Tự động cộng tiền hoàn vào số dư ví của sinh viên.

#### 3.3. Nhóm API Thông Báo Đẩy Firebase Cloud Messaging (FCM)
* **POST `/api/v1/users/me/fcm-token`**:
  * **Mô tả**: Đồng bộ Registration Token của thiết bị di động hoặc Web Browser lên hồ sơ người dùng.
  * **Request Body**:
    ```json
    {
      "fcmToken": "fcm_ktx_dev_token_18b5c90a12",
      "deviceType": "ANDROID",
      "timestamp": "2026-11-05T00:50:00Z"
    }
    ```

* **POST `/api/v1/notifications/push-test`**:
  * **Mô tả**: Gửi thông báo thử nghiệm tới thiết bị của người dùng để kiểm tra việc vẽ banner thông báo Foreground/Background.

---

### 4. QUY TRÌNH KIỂM THỬ TỔNG THỂ (END-TO-END VERIFICATION)
1. **Kiểm thử trên thiết bị thật (Physical Device)**:
   * Mở app -> Nhấn biểu tượng cài đặt máy chủ -> Chọn hoặc nhập IP VPS -> Nhấn **"Kiểm tra kết nối (Ping Test)"** -> Nhận thông báo **"Kết nối thành công (Pong!)"**.
2. **Kiểm thử Force Crash (Firebase Crashlytics)**:
   * Vào tab Cá nhân (S13) -> Nhấn nút **"Kích hoạt Fatal Crash"** màu đỏ -> Xác nhận -> App sập văng ra màn hình chính điện thoại -> Mở lại app -> Báo cáo sự cố được đồng bộ lên Firebase Console.
3. **Kiểm thử Web Admin Portal**:
   * Mở trình duyệt Web (Chrome) truy cập ứng dụng -> Hệ thống tự động vào giao diện Web Admin -> Xem chỉ số KPI -> Mở Trọng tài Khiếu nại -> Xem ảnh bằng chứng phóng to -> Nhấn duyệt hoàn tiền.
