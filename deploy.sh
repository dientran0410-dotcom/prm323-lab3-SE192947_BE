#!/bin/bash
# ========================================================
# Script Tự Động Triển Khai Backend Lên VPS (Docker)
# KTX Food Ordering - PRM393 Lab 3
# ========================================================

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}====================================================${NC}"
echo -e "${YELLOW}   🚀 BẮT ĐẦU QUY TRÌNH DEPLOY BACKEND TRÊN VPS   ${NC}"
echo -e "${YELLOW}====================================================${NC}"

# 1. Kiểm tra Docker & Docker Compose
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Lỗi: Docker chưa được cài đặt trên VPS.${NC}"
    echo "Vui lòng cài Docker bằng lệnh: curl -fsSL https://get.docker.com | sh"
    exit 1
fi

# 2. Kiểm tra file .env
if [ ! -f .env ]; then
    echo -e "${YELLOW}⚠️ Chưa tìm thấy file .env! Đang tự động sao chép từ .env.example...${NC}"
    cp .env.example .env
    echo -e "${YELLOW}👉 Hãy mở file .env bằng lệnh 'nano .env' để cập nhật thông tin Database Online trước khi chạy chính thức.${NC}"
fi

# 3. Pull code mới nhất (nếu là git repo)
if [ -d .git ]; then
    echo -e "${GREEN}🔄 Đang kiểm tra và pull code mới nhất từ Git...${NC}"
    git pull origin main || git pull || true
fi

# 4. Build và khởi động Container
echo -e "${GREEN}📦 Đang tiến hành build image và khởi chạy container...${NC}"
docker compose down || true
docker compose up -d --build

# 5. Chờ hệ thống khởi động và Healthcheck
echo -e "${YELLOW}⏳ Đang chờ Backend khởi động (khoảng 15-25 giây)...${NC}"
sleep 15

# 6. Kiểm tra Health Endpoint
echo -e "${GREEN}🔍 Đang kiểm tra trạng thái hoạt động (/api/v1/health)...${NC}"
MAX_RETRIES=10
COUNT=0
HEALTH_URL="http://localhost:8080/api/v1/health"

while [ $COUNT -lt $MAX_RETRIES ]; do
    if curl -s -f "$HEALTH_URL" > /dev/null; then
        echo -e "${GREEN}✅ BACKEND ĐÃ HOẠT ĐỘNG THÀNH CÔNG!${NC}"
        echo -e "${GREEN}----------------------------------------------------${NC}"
        curl -s "$HEALTH_URL" | grep -o '"status":"[^"]*"' || echo "Status: UP"
        echo -e "${GREEN}----------------------------------------------------${NC}"
        echo -e "👉 Swagger UI nội bộ: http://localhost:8080/swagger-ui/index.html"
        echo -e "👉 Hãy đảm bảo Cloudflare Tunnel trỏ về http://localhost:8080"
        exit 0
    fi
    COUNT=$((COUNT + 1))
    echo "Đang thử lại kết nối ($COUNT/$MAX_RETRIES)..."
    sleep 5
done

echo -e "${RED}⚠️ Backend đang khởi động chậm hoặc gặp sự cố. Kiểm tra log bằng lệnh:${NC}"
echo "docker compose logs -f backend"
exit 1

