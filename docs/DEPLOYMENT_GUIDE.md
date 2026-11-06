# 🚀 Hướng Dẫn Triển Khai Backend Lên VPS (Docker + Cloudflare Tunnel + DB Online)

Tài liệu này hướng dẫn từng bước triển khai hệ thống **Backend Spring Boot (Java 21)** lên máy chủ **VPS** sử dụng **Docker Compose**, mở kết nối an toàn qua **Cloudflare Tunnel (HTTPS miễn phí)** và kết nối với **Database Online**.

---

## 📐 Kiến Trúc Triển Khai

```text
[ Flutter Mobile / Web ]
           │
     (HTTPS) 443
           ▼
[ Cloudflare Edge CDN & Tunnel ]
           │
     (Zero-Trust Ingress)
           ▼
[ VPS: localhost:8080 ] ──► [ Docker Container: order_food_backend (Java 21) ]
                                          │
                                    (JDBC SSL / 3306)
                                          ▼
                         [ Cloud MySQL Online (Aiven / TiDB / Clever Cloud) ]
```

---

## 🛠️ Bước 1: Chuẩn Bị Database Online (Miễn Phí)

Dự án sử dụng driver `mysql-connector-j` chuẩn MySQL 8. Bạn có thể chọn 1 trong các nhà cung cấp DB Online miễn phí sau:

1. **Aiven for MySQL (Khuyên dùng):**
   * Đăng ký tại: [aiven.io](https://aiven.io/)
   * Tạo Free Plan service MySQL 8 (hoàn toàn miễn phí, hỗ trợ SSL tự động).
   * Lấy thông tin: `Host`, `Port` (thường dạng `xxxxx.aivencloud.com:xxxxx`), `User`, `Password`, `Database name` (`defaultdb`).
2. **TiDB Cloud (Serverless):**
   * Đăng ký tại: [tidbcloud.com](https://tidbcloud.com/)
   * Tương thích 100% giao thức MySQL 8, free tier 5GB lưu trữ.

---

## 🐳 Bước 2: Triển Khai Lên VPS Bằng Docker Compose

### 1. Đưa mã nguồn lên VPS
Trên máy tính cá nhân hoặc VPS, bạn clone repository hoặc copy thư mục `order_food_backend` lên VPS:
```bash
# Ví dụ copy từ local lên VPS qua rsync/scp hoặc git pull
cd /opt/order_food_backend
```

### 2. Tạo file cấu hình biến môi trường `.env`
Sao chép từ file mẫu `.env.example`:
```bash
cp .env.example .env
nano .env
```

Điền các thông tin của Database Online bạn đã tạo ở Bước 1:
```ini
SPRING_DATASOURCE_URL=jdbc:mysql://YOUR_DB_HOST:YOUR_DB_PORT/YOUR_DB_NAME?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
SPRING_DATASOURCE_USERNAME=YOUR_DB_USERNAME
SPRING_DATASOURCE_PASSWORD=YOUR_DB_PASSWORD
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.MySQLDialect
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_H2_CONSOLE_ENABLED=false
```

> **Mẹo:** Nếu chưa muốn tạo DB Online ngay, bạn có thể chạy MySQL container ngay trên VPS bằng cách uncomment phần `LỰA CHỌN 2` trong `.env` và chạy với `--profile with-mysql`.

### 3. Khởi chạy Docker Container
```bash
# Build và chạy ngầm container
docker compose up -d --build

# Xem log khởi động của Spring Boot
docker compose logs -f backend
```

Khi thấy dòng log:
`Started OrderAppBackendApplication in X.XXX seconds (process running for ...)`
là backend đã chạy thành công trên cổng `8080`.

### 4. Kiểm tra sức khỏe hệ thống (Health Check)
Chạy lệnh kiểm tra ngay trên VPS:
```bash
curl http://localhost:8080/api/v1/health
```
Kết quả trả về JSON có `"status": "UP"` là hoàn tất!

---

## 🌐 Bước 3: Cấu Hình Cloudflare Tunnel (HTTPS Public)

Cloudflare Tunnel giúp bạn public backend ra Internet an toàn mà **không cần mở port firewall (UFW)** trên VPS.

### Cách 1: Cấu hình qua giao diện Cloudflare Zero Trust (Đơn giản nhất)
1. Đăng nhập [dash.cloudflare.com](https://dash.cloudflare.com/) ➔ Chọn **Zero Trust** ➔ **Networks** ➔ **Tunnels**.
2. Chọn Tunnel đang chạy trên VPS của bạn ➔ Bấm **Configure**.
3. Chọn tab **Public Hostname** ➔ Bấm **Add a public hostname**:
   * **Subdomain:** `api-food` (hoặc tên tùy thích)
   * **Domain:** `yourdomain.com` (tên miền bạn đã trỏ về Cloudflare)
   * **Type:** `HTTP`
   * **URL:** `localhost:8080`
4. Bấm **Save hostname**.

### Cách 2: Cấu hình qua file `config.yml` của `cloudflared` trên VPS
Thêm ingress rule vào `/etc/cloudflared/config.yml`:
```yaml
ingress:
  - hostname: api-food.yourdomain.com
    service: http://localhost:8080
  - service: http_status:404
```
Khởi động lại service cloudflared:
```bash
sudo systemctl restart cloudflared
```

Bây giờ bạn có thể truy cập Swagger UI hoặc Health Check từ bất cứ đâu qua link:
`https://api-food.yourdomain.com/swagger-ui/index.html` hoặc `https://api-food.yourdomain.com/api/v1/health`

---

## 📱 Bước 4: Kết Nối Ứng Dụng Flutter Vào Backend Mới

Sau khi backend đã online tại `https://api-food.yourdomain.com/api/v1`, bạn có 2 cách để app Flutter kết nối:

### Cách 1: Thiết lập trực tiếp trong App Flutter qua giao diện UI
1. Mở app Flutter ➔ Vào màn hình **Cài đặt** (Settings) hoặc bấm vào nút **Cấu hình máy chủ**.
2. Nhập URL: `https://api-food.yourdomain.com/api/v1` ➔ Bấm **Lưu & Kiểm tra kết nối**.

### Cách 2: Đổi URL mặc định trong code
Mở file `lib/services/api_config.dart` và cập nhật:
```dart
static String _dynamicBaseUrl = 'https://api-food.yourdomain.com/api/v1';
```

---

## 📋 Tóm Tắt Lệnh Thường Dùng Trên VPS

| Tác vụ | Lệnh |
|---|---|
| Khởi động backend | `docker compose up -d` |
| Xem log trực tiếp | `docker compose logs -f backend` |
| Dừng hệ thống | `docker compose down` |
| Rebuild khi cập nhật code | `docker compose up -d --build` |
| Kiểm tra trạng thái container | `docker compose ps` |
