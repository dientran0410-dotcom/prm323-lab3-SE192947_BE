#!/bin/bash
# ==============================================================================
# SCRIPT THEO DÕI VÀ BẮT MÃ OTP TỰ ĐỘNG TRÊN VPS KTX FOOD
# Khi sinh viên bấm "Gửi mã xác thực" trên App, mã OTP sẽ nhảy ra ngay lập tức!
#
# Thứ tự nguồn đọc OTP:
#   1. data/latest_otp.txt           (thư mục ./data được mount từ Docker ra host)
#   2. latest_otp.txt                (khi chạy backend bằng java -jar / mvnw)
#   3. docker exec cat file trong container (backend bản cũ chưa rebuild)
# ==============================================================================

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Luôn chạy tại thư mục chứa script (để đường dẫn tương đối data/ luôn đúng)
cd "$(dirname "$0")" || exit 1

CONTAINER="${OTP_CONTAINER:-order_food_backend}"
HAS_DOCKER=0
if command -v docker >/dev/null 2>&1; then
    HAS_DOCKER=1
fi

read_otp() {
    local f
    for f in "data/latest_otp.txt" "latest_otp.txt"; do
        if [ -s "$f" ]; then
            tr -d '\r' < "$f"
            return 0
        fi
    done
    if [ "$HAS_DOCKER" -eq 1 ]; then
        local v
        v=$(docker exec "$CONTAINER" sh -c 'cat /app/data/latest_otp.txt 2>/dev/null || cat /app/latest_otp.txt 2>/dev/null' 2>/dev/null | tr -d '\r')
        if [ -n "$v" ]; then
            echo "$v"
            return 0
        fi
    fi
    return 1
}

clear
echo -e "${CYAN}======================================================================${NC}"
echo -e "${BOLD}${YELLOW}   🚀 KTX FOOD - BỘ GIÁM SÁT MÃ XÁC THỰC OTP TRÊN VPS SERVER${NC}"
echo -e "${CYAN}======================================================================${NC}"
echo -e "Đang theo dõi mã OTP thời gian thực... (Nhấn Ctrl+C để thoát)"
echo ""

LAST_VAL=""
FIRST=$(read_otp)
if [ -n "$FIRST" ]; then
    LAST_VAL="$FIRST"
    echo -e "${GREEN}📌 MÃ OTP GẦN ĐÂY NHẤT LÀ:${NC}"
    echo -e "${BOLD}${YELLOW}${FIRST}${NC}"
    echo -e "${CYAN}----------------------------------------------------------------------${NC}"
fi

while true; do
    CURR_VAL=$(read_otp)
    if [ -n "$CURR_VAL" ] && [ "$CURR_VAL" != "$LAST_VAL" ]; then
        LAST_VAL="$CURR_VAL"
        echo ""
        echo -e "${RED}======================================================================${NC}"
        echo -e "${BOLD}${GREEN}🔔 PHÁT HIỆN YÊU CẦU ĐỔI MẬT KHẨU MỚI TỪ APP:${NC}"
        echo -e "${BOLD}${YELLOW}👉 ${CURR_VAL}${NC}"
        echo -e "${RED}======================================================================${NC}"
        echo ""
    fi
    sleep 1
done
