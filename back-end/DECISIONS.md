# DECISIONS.md — Quyết định thiết kế

# Ghi lại các quyết định kỹ thuật quan trọng để AI không làm ngược lại

---

## [2024-01] Dùng JWT stateless, không dùng Session

**Quyết định:** Xác thực bằng JWT, không lưu session phía server.

**Lý do:**
- API sẽ được gọi từ mobile app sau này
- Dễ scale ngang khi cần nhiều server
- Không phụ thuộc vào shared session storage

**Phương án bị loại:** HttpSession — không phù hợp khi scale

**Ràng buộc:** Token hết hạn sau 24 giờ. Refresh token lưu trong DB.

---

## [2024-01] Dùng DTO tách biệt Request và Response

**Quyết định:** Tạo riêng XxxRequest và XxxResponse, không dùng Entity.

**Lý do:**
- Tránh lộ thông tin nhạy cảm (password hash, internal ID...)
- Dễ thay đổi API mà không ảnh hưởng database
- Validation ở tầng DTO, không ở Entity

**Ví dụ:**
ProductRequest  → nhận dữ liệu từ client
ProductResponse → trả về cho client  
Product (Entity) → chỉ dùng nội bộ trong service/repo

---

## [2024-01] Soft delete thay vì xóa thật

**Quyết định:** Thêm cột `is_deleted` và `deleted_at`, không xóa record khỏi DB.

**Lý do:**
- Đơn hàng cũ vẫn cần tham chiếu đến sản phẩm đã "xóa"
- Có thể khôi phục dữ liệu khi cần
- Audit trail cho admin

**Cách implement:** Dùng @Where(clause = "is_deleted = 0") trên Entity

---

## [2024-01] Phân trang mặc định 20 items

**Quyết định:** Tất cả API trả danh sách đều phân trang, mặc định 20 items/trang.

**Lý do:** Tránh load toàn bộ data khi có nhiều sản phẩm.

**Cách gọi:** GET /api/products?page=0&size=20&sort=createdAt,desc

**Giới hạn:** size tối đa 100, nếu client gửi quá 100 thì ép về 100.

---

## [CHƯA QUYẾT ĐỊNH] Cổng thanh toán

**Đang cân nhắc:** VNPay vs MoMo vs COD (thanh toán khi nhận hàng)
**Ghi chú:** Chưa implement module Payment — chờ quyết định.