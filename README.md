# 🍛 KTX Food Ordering System - Backend Service (Spring Boot 4 / Java 21)

Hệ thống Backend phục vụ ứng dụng Đặt cơm KTX Food (PRM393 Lab 3).

## 🚀 Hướng Dẫn Deploy Lên VPS
Tất cả các tài liệu và script tự động phục vụ deploy VPS bằng Docker & Cloudflare Tunnel nằm tại:
* 👉 **[DEPLOY_VPS.md](./DEPLOY_VPS.md)** - Cẩm nang chi tiết từng lệnh thao tác trên VPS.
* 👉 **[deploy.sh](./deploy.sh)** - Script tự động 1 lệnh chạy deploy trên VPS.
* 👉 **[.env.example](./.env.example)** - File mẫu cấu hình Database MySQL Online.
* 👉 **[docker-compose.yml](./docker-compose.yml)** - Cấu hình Docker Compose cho VPS.

## 🛠️ Chạy cục bộ trên máy tính cá nhân (Local Dev)
```bash
./mvnw clean spring-boot:run
```
Swagger UI: http://localhost:8080/swagger-ui/index.html
