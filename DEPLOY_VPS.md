# 📋 HƯỚNG DẪN THAO TÁC TRÊN VPS (COPY & PASTE LÀ CHẠY)
> **Mục tiêu:** Deploy nhanh Backend Spring Boot (Java 21) bằng Docker, kết nối Database Online và cấu hình Cloudflare Tunnel (HTTPS) trên máy chủ VPS.

---

## ⚡ CÁCH 1: CHẠY TỰ ĐỘNG BẰNG 1 LỆNH (NHANH NHẤT)

Trên VPS, mở terminal tại thư mục backend và chạy:
```bash
chmod +x deploy.sh && ./deploy.sh
```
*Script sẽ tự động kiểm tra Docker, tạo `.env` nếu thiếu, build container, chạy ngầm và kiểm tra endpoint `/api/v1/health`.*

---

## 🛠️ CÁCH 2: THAO TÁC THỦ CÔNG TỪNG BƯỚC

### Bước 1: Di chuyển vào thư mục Backend trên VPS
```bash
cd /opt/order_food_backend
# hoặc thư mục bạn vừa clone/copy code vào
```

### Bước 2: Cấu hình kết nối Database Online
Tạo file `.env` từ file mẫu:
```bash
cp .env.example .env
nano .env
```

**Nội dung mẫu file `.env` khi dùng Database MySQL Online (ví dụ Aiven, TiDB, Clever Cloud):**
```ini
SPRING_DATASOURCE_URL=jdbc:mysql://YOUR_DB_HOST:YOUR_DB_PORT/YOUR_DB_NAME?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
SPRING_DATASOURCE_USERNAME=YOUR_DB_USERNAME
SPRING_DATASOURCE_PASSWORD=YOUR_DB_PASSWORD
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.MySQLDialect
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_H2_CONSOLE_ENABLED=false
```
*(Ấn `Ctrl + O` rồi `Enter` để lưu, sau đó `Ctrl + X` để thoát `nano`)*

---

### Bước 3: Build và Khởi động Container Docker
```bash
# Tắt container cũ (nếu có) và khởi động bản mới nhất
docker compose down
docker compose up -d --build
```

### Bước 4: Kiểm tra trạng thái và Logs
```bash
# 1. Xem trạng thái container (cần thấy State là Up (healthy))
docker compose ps

# 2. Xem trực tiếp log khởi động của Spring Boot
docker compose logs -f backend

# 3. Test nhanh endpoint sức khỏe (Health Check)
curl http://localhost:8080/api/v1/health
```
Khi thấy kết quả trả về JSON có `"status":"UP"` nghĩa là backend đã hoạt động hoàn hảo!

---

## 🌐 BƯỚC 5: CẤU HÌNH CLOUDFLARE TUNNEL (PUBLIC HTTPS)

VPS đã cài sẵn Cloudflare Tunnel, bạn chỉ cần liên kết domain vào port `8080`:

### Lựa chọn A: Cấu hình qua giao diện Web Cloudflare (Khuyên dùng)
1. Đăng nhập [dash.cloudflare.com](https://dash.cloudflare.com/) ➔ Chọn **Zero Trust**.
2. Vào **Networks** ➔ **Tunnels** ➔ Chọn Tunnel VPS của bạn ➔ Bấm **Configure**.
3. Chọn tab **Public Hostname** ➔ Bấm **Add a public hostname**.
4. Điền các trường:
   - **Subdomain:** `api-food` (hoặc tên bạn muốn, ví dụ `api`)
   - **Domain:** chọn tên miền của bạn (ví dụ `example.com`)
   - **Type:** `HTTP`
   - **URL:** `localhost:8080`
5. Bấm **Save hostname**.

### Lựa chọn B: Cấu hình bằng lệnh / File trên VPS
Nếu bạn quản lý tunnel bằng file `/etc/cloudflared/config.yml`:
```yaml
ingress:
  - hostname: api-food.yourdomain.com
    service: http://localhost:8080
  - service: http_status:404
```
Lưu file và restart service `cloudflared`:
```bash
sudo systemctl restart cloudflared
```

👉 **Kiểm tra từ trình duyệt bất kỳ:**
* Mở: `https://api-food.yourdomain.com/api/v1/health`
* Mở Swagger UI: `https://api-food.yourdomain.com/swagger-ui/index.html`

---

## 📱 BƯỚC 6: CẬP NHẬT FLUTTER APP CLIENT

Khi backend đã có domain HTTPS công khai, mở app Flutter để kết nối:
* **Cách 1 (Ngay trên giao diện App):** Vào mục Cấu hình máy chủ ➔ Nhập `https://api-food.yourdomain.com/api/v1` ➔ Bấm Lưu.
* **Cách 2 (Trong mã nguồn):** Mở file `lib/services/api_config.dart` trong project Flutter:
  ```dart
  static String _dynamicBaseUrl = 'https://api-food.yourdomain.com/api/v1';
  ```

---

## 🧰 CÁC LỆNH BẢO TRÌ & QUẢN LÝ THƯỜNG DÙNG

| Mục đích | Câu lệnh |
|---|---|
| **Xem logs trực tiếp** | `docker compose logs -f backend` |
| **Khởi động lại backend** | `docker compose restart backend` |
| **Dừng hệ thống** | `docker compose down` |
| **Cập nhật code mới từ Git** | `git pull && docker compose up -d --build` |
| **Kiểm tra mức sử dụng RAM/CPU** | `docker stats order_food_backend` |
| **Dọn dẹp rác Docker giải phóng dung lượng VPS** | `docker system prune -f` |

---

## ⚠️ XỬ LÝ SỰ CỐ THƯỜNG GẶP (TROUBLESHOOTING)

1. **Lỗi `Access denied for user...` hoặc không kết nối được Database:**
   * Kiểm tra lại `Host`, `Port`, `Username`, `Password` trong file `.env`.
   * Hãy chắc chắn Database Online (Aiven / TiDB) cho phép IP của VPS truy cập (hoặc đã mở Allow all `0.0.0.0/0` trong firewall của DB).

2. **Lỗi `port is already allocated: 8080`:**
   * Có một process khác đang chiếm cổng 8080 trên VPS.
   * Tìm process: `sudo lsof -i :8080` hoặc `sudo netstat -tulpn | grep 8080`.
   * Tắt process đó đi hoặc đổi port trong `docker-compose.yml` thành `"8081:8080"` (đồng thời đổi cấu hình Cloudflare Tunnel trỏ về `localhost:8081`).

3. **RAM VPS bị đầy khi build Docker:**
   * Nếu VPS có RAM 1GB hoặc 2GB, hãy tạo swap file 2GB để quá trình Maven build không bị crash (OOM):
     ```bash
     sudo fallocate -l 2G /swapfile
     sudo chmod 600 /swapfile
     sudo mkswap /swapfile
     sudo swapon /swapfile
     ```

