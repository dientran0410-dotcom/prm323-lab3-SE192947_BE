# QUY TẮC NGHIỆP VỤ HỆ THỐNG (BUSINESS RULES SPECIFICATION)

- **Dự án:** Food Ordering App (PRM393 - Lab 3)
- **Mục đích:** Quy định toàn bộ quy tắc nghiệp vụ bất biến của hệ thống. Backend Service là nơi chịu trách nhiệm thực thi và bảo vệ các quy tắc này.

---

## 1. Phân hệ Đơn hàng (Order Business Rules - BR-ORD)

### BR-ORD-01: Ràng buộc Giỏ hàng & Số lượng Món
- Đơn hàng tạo mới bắt buộc phải chứa ít nhất 01 món ăn hợp lệ (`items.size() >= 1`).
- Số lượng từng món trong đơn phải là số nguyên dương (`quantity >= 1`).
- Món ăn được đặt phải đang ở trạng thái kinh doanh (`menu_item.is_available == true`) và quán ăn đang mở cửa (`store.is_active == true`).

### BR-ORD-02: Bất biến Giá (Price Snapshot Immutability)
- Khi đơn hàng được xác nhận tạo thành công, toàn bộ đơn giá gốc (`unitPrice`) và phụ thu tùy biến (`extraPrice` cho Size, Topping) của từng món ăn phải được ghi nhận cố định vào bảng `order_items`.
- Tuyệt đối không tính toán lại giá đơn hàng dựa trên bảng `menu_items` sau khi đơn hàng đã hình thành. Mọi biến động giá của quán trong tương lai không ảnh hưởng tới đơn hàng cũ.

### BR-ORD-03: Gom đơn theo Ca & Chia sẻ Phí Giao hàng KTX
- Mỗi đơn hàng bắt buộc phải gắn liền với một Ca gom đơn cố định (`slotTime`), ví dụ: `CA_TRUA_11H30`, `CA_CHIEU_18H00`.
- Phí giao hàng tiêu chuẩn được tính theo chính sách gom đơn:
  - Nếu số lượng đơn trong cùng ca tại cùng Trụ/Cổng KTX $\ge 5$ đơn $\rightarrow$ Phí ship giảm còn $5,000\text{ VND}$/đơn (hoặc miễn phí theo Remote Config).
  - Phí giao hàng đơn lẻ tiêu chuẩn: $15,000\text{ VND}$.

### BR-ORD-04: Điểm Giao Nhận Chuẩn hóa tại KTX
- Điểm giao nhận (`pickupLocation`) phải tuân theo các vị trí thực tế tại KTX ĐHQG / FPT đã được số hóa:
  - `KTX Khu B – Trụ A1–A2 (Cổng B2)`
  - `KTX Khu B – Nhà xe Tòa BA1`
  - `KTX Khu A – Sảnh Tòa A3`
  - `KTX Khu A – Cổng chính Đường Quảng Trường`

### BR-ORD-05: Định danh Đối soát Nhận hàng (Pickup QR & PIN)
- Mỗi đơn hàng khi chuyển sang trạng thái `READY` hoặc `DELIVERING` sẽ tự động phát sinh:
  - 01 mã hash ngẫu nhiên dùng để render mã QR đối soát (`pickupQrCode`).
  - 01 mã PIN 4 số (mặc định lấy 4 số cuối số điện thoại sinh viên hoặc mã OTP sinh ngẫu nhiên).
- Khi shipper và sinh viên gặp nhau tại trụ KTX:
  - Luồng ưu tiên: Shipper quét mã QR trên màn hình sinh viên (S7) $\rightarrow$ Hệ thống tự động chuyển đơn sang `DELIVERED`.
  - Luồng thay thế (khi chói nắng hoặc camera hỏng): Sinh viên đọc mã PIN $\rightarrow$ Shipper nhập mã PIN trên ứng dụng $\rightarrow$ Chuyển trạng thái sang `DELIVERED`.

### BR-ORD-06: Chuyển đổi Trạng thái Đơn hàng (State Machine)
- Trạng thái chỉ được chuyển đổi theo chiều tiến hợp lệ:
  - `PENDING` $\rightarrow$ `CONFIRMED` $\rightarrow$ `PREPARING` $\rightarrow$ `READY` $\rightarrow$ `DELIVERING` $\rightarrow$ `DELIVERED`.
- Quy tắc hủy đơn:
  - Sinh viên chỉ được tự hủy đơn khi trạng thái là `PENDING`.
  - Khi đơn đã sang `PREPARING` (quán đang nấu) hoặc `DELIVERING` hoặc `DELIVERED`, sinh viên không được phép hủy đơn.
  - Quán ăn hoặc Quản trị viên có quyền chuyển sang `CANCELLED` khi hết nguyên liệu hoặc sự cố bất khả kháng.

---

## 2. Phân hệ Thanh toán (Payment Business Rules - BR-PAY)

### BR-PAY-01: Thanh toán VietQR Chuẩn hóa
- Hệ thống hỗ trợ sinh mã VietQR theo chuẩn Napas 247 qua `VietQrPaymentGateway`.
- Chuỗi nội dung chuyển khoản bắt buộc theo định dạng: `ORD_<ORDER_ID>` để phục vụ đối soát giao dịch tự động.
- Số tiền trên mã QR phải khớp chính xác tuyệt đối với `finalAmount` của đơn hàng.

### BR-PAY-02: Cơ chế Dự phòng Chuyển sang Tiền mặt (COD Fallback)
- Tại màn hình thanh toán (S5), nếu người dùng gặp sự cố với App ngân hàng hoặc hủy quét VietQR, hệ thống cho phép kích hoạt API `switch-to-cod`.
- Phương thức thanh toán của đơn hàng sẽ chuyển từ `VIETQR` sang `COD`, trạng thái thanh toán là `UNPAID` và trạng thái đơn chuyển sang `CONFIRMED`.

---

## 3. Phân hệ Khiếu nại & Hoàn tiền (Dispute & Refund - BR-DIS)

### BR-DIS-01: Điều kiện Khởi tạo Khiếu nại
- Chỉ những đơn hàng đã hoàn tất giao hàng (`status == 'DELIVERED'`) mới được quyền gửi khiếu nại.
- Mỗi đơn hàng chỉ được tạo tối đa 01 khiếu nại (`order_id` là Unique trong bảng `disputes`).
- Thời hạn khiếu nại: Trong vòng 24 giờ kể từ thời điểm nhận hàng (`deliveredAt`).

### BR-DIS-02: Ràng buộc Bằng chứng Hình ảnh (Mandatory Evidence)
- Mọi khiếu nại bắt buộc phải đính kèm ít nhất 01 URL ảnh chụp thực tế bằng chứng món ăn bị lỗi/hỏng/thiếu (`evidenceImages.size() >= 1`).
- Nếu request gửi lên không có ảnh, hệ thống lập tức từ chối và trả về mã lỗi `DISPUTE_EVIDENCE_REQUIRED` (HTTP 400).

### BR-DIS-03: Thẩm định & Giới hạn Số tiền Hoàn (Refund Calculation)
- Backend chịu trách nhiệm kiểm tra tính hợp lệ của số tiền yêu cầu hoàn (`refundRequestedAmount`):
  - $0 < \text{refundRequestedAmount} \le \text{finalAmount}$ của đơn hàng.
  - Tuyệt đối không chấp thuận số tiền hoàn vượt quá tổng giá trị đơn hàng sinh viên đã trả.
- Trạng thái xử lý khiếu nại: `PENDING` $\rightarrow$ `INVESTIGATING` $\rightarrow$ `APPROVED` hoặc `REJECTED`. Khi `APPROVED`, hệ thống ghi nhận `refundApprovedAmount` và kích hoạt luồng hoàn tiền.

---

## 4. Phân hệ Đánh giá (Review Business Rules - BR-REV)

### BR-REV-01: Điều kiện Đánh giá Dịch vụ
- Chỉ sinh viên là chủ sở hữu đơn hàng mới có quyền đánh giá đơn hàng đó.
- Đơn hàng phải ở trạng thái `DELIVERED`.
- Mỗi đơn hàng chỉ được đánh giá duy nhất 01 lần (1-to-1 relationship).

### BR-REV-02: Thang điểm Đánh giá
- Điểm đánh giá Quán ăn (`storeRating`): Số nguyên từ 1 đến 5 sao.
- Điểm đánh giá Shipper giao hàng (`shipperRating`): Số nguyên từ 1 đến 5 sao.
- Khi một đánh giá mới được lưu, hệ thống tự động cập nhật lại `rating_avg` và `rating_count` của quán ăn tương ứng.

---

## 5. Phân hệ Bảo mật & Quyền riêng tư (Security Rules - BR-SEC)

### BR-SEC-01: Quyền Sở hữu Dữ liệu (Data Ownership)
- Sinh viên chỉ có quyền truy xuất thông tin chi tiết, lịch sử đơn hàng, lịch sử khiếu nại thuộc về chính tài khoản của mình (`user_id == currentUserId`).
- Quản trị viên (`ADMIN`) có quyền xem toàn bộ dữ liệu. Chủ quán ăn (`STORE_OWNER`) chỉ có quyền xem đơn hàng của quán mình.
- Bất kỳ hành vi truy cập trái phép đơn hàng của người khác đều bị chặn với mã lỗi `ACCESS_DENIED` (HTTP 403).

---

## 6. Phân hệ Thông báo Đơn hàng & Push FCM (Notification Business Rules - BR-NOTI)

### BR-NOTI-01: Quyền Truy cập & Thao tác Thông báo
- Sinh viên chỉ có quyền xem danh sách thông báo và cập nhật trạng thái đã đọc trên các thông báo thuộc về chính tài khoản của mình (`user_id == currentUserId`).
- Bất kỳ hành vi đánh dấu đã đọc trên thông báo của người dùng khác đều bị chặn với mã lỗi `ACCESS_DENIED` (HTTP 403).

### BR-NOTI-02: Cập nhật Trạng thái Đã đọc (Read Status)
- Cho phép đánh dấu đã đọc cho từng thông báo cụ thể (`PATCH /api/v1/notifications/{id}/read`).
- Cho phép đánh dấu đã đọc hàng loạt toàn bộ thông báo chưa đọc của sinh viên (`PATCH /api/v1/notifications/read-all`).
- Cung cấp API đếm nhanh số lượng thông báo chưa đọc (`GET /api/v1/notifications/unread-count`) phục vụ hiển thị badge số đỏ trên icon chuông của ứng dụng Flutter.

### BR-NOTI-03: Điều phối Thông báo Trạng thái (Event Dispatching)
- Hỗ trợ gửi thông báo theo các phân loại chuẩn (`ORDER_STATUS`, `DISPUTE_UPDATE`, `PAYMENT_UPDATE`, `SYSTEM`) kèm dữ liệu nhúng JSON `dataPayload`.
- Sẵn sàng kích hoạt Firebase Cloud Messaging (FCM) push notification khi tích hợp thiết bị di động.

