#!/bin/bash
# ==============================================================================
# SCRIPT LẤY NHANH MÃ OTP ĐỔI MẬT KHẨU
# Cách dùng: ./get_otp.sh hoặc ./get_otp.sh 0968097907
# ==============================================================================

PHONE="${1:-0968097907}"

echo "=========================================================="
echo "🔍 Đang tra cứu mã OTP cho số: $PHONE..."

# Cách 1: Đọc từ file latest_otp.txt nếu có
if [ -f "latest_otp.txt" ]; then
    echo "📁 Dữ liệu từ file latest_otp.txt:"
    cat latest_otp.txt
    echo ""
fi

# Cách 2: Gọi API Backend cục bộ port 8080
RESPONSE=$(curl -s "http://localhost:8080/api/v1/auth/forgot-password/latest-otp?phoneNumber=$PHONE")

if echo "$RESPONSE" | grep -q '"success":true'; then
    OTP=$(echo "$RESPONSE" | grep -o '"otp":"[^"]*' | cut -d'"' -f4)
    echo "=========================================================="
    echo "🎉 TÌM THẤY MÃ OTP ĐANG HOẠT ĐỘNG:"
    echo "👉 MÃ OTP: >>> $OTP <<<"
    echo "=========================================================="
else
    echo "⚠️ Không tìm thấy OTP qua API (hoặc đã hết hạn 5 phút)."
    echo "💡 Gợi ý: Bạn có thể nhập mã test mặc định '123456' trên App."
    echo "=========================================================="
fi
