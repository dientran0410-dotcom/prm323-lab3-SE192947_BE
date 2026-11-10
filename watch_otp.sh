#!/bin/bash
# ==============================================================================
# SCRIPT THEO DÕI VÀ BẮT MÃ OTP TỰ ĐỘNG TRÊN VPS KTX FOOD
# Khi sinh viên bấm "Gửi mã xác thực" trên App, mã OTP sẽ nhảy ra ngay lập tức!
# ==============================================================================

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

clear
echo -e "${CYAN}======================================================================${NC}"
echo -e "${BOLD}${YELLOW}   🚀 KTX FOOD - BỘ GIÁM SÁT MÃ XÁC THỰC OTP TRÊN VPS SERVER${NC}"
echo -e "${CYAN}======================================================================${NC}"
echo -e "Đang theo dõi mã OTP thời gian thực... (Nhấn Ctrl+C để thoát)"
echo ""

# Nếu đã có mã OTP trước đó, hiển thị luôn ngay khi vừa mở script
if [ -f "latest_otp.txt" ]; then
    echo -e "${GREEN}📌 MÃ OTP GẦN ĐÂY NHẤT LÀ:${NC}"
    echo -e "${BOLD}${YELLOW}$(cat latest_otp.txt)${NC}"
    echo -e "${CYAN}----------------------------------------------------------------------${NC}"
elif [ -f "/tmp/latest_otp.txt" ]; then
    echo -e "${GREEN}📌 MÃ OTP GẦN ĐÂY NHẤT LÀ:${NC}"
    echo -e "${BOLD}${YELLOW}$(cat /tmp/latest_otp.txt)${NC}"
    echo -e "${CYAN}----------------------------------------------------------------------${NC}"
fi

LAST_VAL=""

# Vòng lặp liên tục quét mã OTP mới từ cả 3 nguồn:
# 1. File latest_otp.txt / /tmp/latest_otp.txt
# 2. File otp_history.log
# 3. Trực tiếp từ API local: http://localhost:8080/api/v1/auth/forgot-password/latest-otp
while true; do
    CURR_VAL=""
    if [ -f "latest_otp.txt" ]; then
        CURR_VAL=$(cat latest_otp.txt 2>/dev/null)
    elif [ -f "/tmp/latest_otp.txt" ]; then
        CURR_VAL=$(cat /tmp/latest_otp.txt 2>/dev/null)
    fi

    if [ -n "$CURR_VAL" ] && [ "$CURR_VAL" != "$LAST_VAL" ]; then
        LAST_VAL="$CURR_VAL"
        echo ""
        echo -e "${RED}======================================================================${NC}"
        echo -e "${BOLD}${GREEN}🔔 PHÁT HIỆN YÊU CẦU ĐỔI MẬT KHẨU MỚI TỪ APP:${NC}"
        echo -e "${BOLD}${YELLOW}👉 $CURR_VAL${NC}"
        echo -e "${RED}======================================================================${NC}"
        echo ""
    fi
    sleep 1
done

