#!/usr/bin/env python3
import time
import os
import sys
import json
import urllib.request

def main():
    print("=" * 70)
    print("🚀 KTX FOOD - BỘ GIÁM SÁT MÃ OTP THỜI GIAN THỰC")
    print("=" * 70)
    
    last_content = ""
    target_files = ["latest_otp.txt", "/tmp/latest_otp.txt"]
    
    # Check if existing file has OTP
    for f in target_files:
        if os.path.exists(f):
            try:
                with open(f, "r", encoding="utf-8") as fp:
                    content = fp.read().strip()
                    if content:
                        print(f"📌 MÃ OTP GẦN NHẤT:\n👉 {content}")
                        print("-" * 70)
                        last_content = content
                        break
            except Exception:
                pass

    print("Đang lắng nghe mã OTP mới... (Nhấn Ctrl+C để dừng)")
    
    while True:
        try:
            curr = ""
            for f in target_files:
                if os.path.exists(f):
                    try:
                        with open(f, "r", encoding="utf-8") as fp:
                            curr = fp.read().strip()
                            if curr:
                                break
                    except Exception:
                        pass
            
            if curr and curr != last_content:
                last_content = curr
                print("\n" + "=" * 70)
                print("🔔 PHÁT HIỆN YÊU CẦU ĐỔI MẬT KHẨU MỚI:")
                print(f"👉 {curr}")
                print("=" * 70 + "\n")
            
            time.sleep(1)
        except KeyboardInterrupt:
            print("\nĐã dừng giám sát.")
            break
        except Exception:
            time.sleep(1)

if __name__ == "__main__":
    main()
