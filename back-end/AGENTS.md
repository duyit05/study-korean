## Dự án là gì
Web quản lý và học tiếng hàn: quản lý theo các entity đã được định nghĩa trong database


## Tech Stack
- Java 17
- Spring Boot 3.5.16
- Spring Security + JWT
- Spring Data JPA + Hibernate
- postgresql (database: )
- Maven
- Lombok (dùng @Data, @Builder, @RequiredArgsConstructor) -> tùy thuộc vào nghiệp vụ

## Cấu trúc thư mục
src/main/java/com/example/shop/
├── controller/     — REST API endpoints
├── service/        — Business logic
├── repository/     — JPA repositories
├── entity/         — Database entities
├── dto/            — Request/Response objects
├── config/         — Security, CORS config
└── exception/      — Global exception handler

## Cách chạy project
mvn spring-boot:run
Port mặc định: 8080
Database config trong: src/main/resources/application.yml

## Cách chạy test
mvn test                    — chạy tất cả test
mvn test -Dtest=TênTest     — chạy test cụ thể

## Quy tắc BẮT BUỘC (không được vi phạm)
1. Mọi API đều phải xác thực JWT — trừ /api/auth/**
2. Không viết logic trong Controller — chuyển hết vào Service
3. Dùng DTO cho request/response, không expose Entity trực tiếp
4. Mọi lỗi phải trả về qua GlobalExceptionHandler
5. Tên biến, method dùng tiếng Anh — comment có thể tiếng Việt
6. Viết test cho mọi Service method quan trọng
7. Tuyệt đối không để xảy ra lỗi N+1 Query: Cấu hình mặc định FetchType.LAZY cho các quan hệ @OneToOne và @ManyToOne. Khi cần fetch dữ liệu liên quan, bắt buộc sử dụng JOIN FETCH hoặc @EntityGraph trong JPQL.
8. Tránh lỗi lặp truy vấn (Duplicate Queries) và Over-fetching:
    - Thông tin User hiện tại phải được lấy trực tiếp từ SecurityContextHolder, tuyệt đối không query lại DB bằng username/email ở các tầng Service bên dưới.
    - Không được gọi query đơn lẻ hoặc tính toán kích thước danh sách (.size()) trong các vòng lặp. Phải sử dụng truy vấn đếm gom (COUNT) hoặc nạp dữ liệu theo lô (Batch fetching) bằng toán tử IN.
    - Khi trả về các API danh sách (List DTO), chỉ bao gồm các trường đếm số lượng (Ví dụ: wordCount, questionCount). Không lôi các tập hợp dữ liệu con chi tiết (nested lists) ra ngoài trừ khi gọi API chi tiết riêng.
9. Quy tắc phát triển Frontend (Vue.js + Pinia):
    - Quản lý API qua State Management: Tất cả các API call bắt buộc phải được thực hiện bên trong Actions của Pinia Store. Các file Component của Vue (`.vue`) tuyệt đối không gọi trực tiếp API (qua axios/fetch), mà chỉ import Store, khai báo sử dụng Store và gọi các Action hoặc getter tương ứng.
    - Đồng bộ hóa Giao diện (UI Consistency): Tất cả các thành phần giao diện như nút bấm (button), biểu mẫu (form), ô chọn (select option), bảng biểu (table)... phải sử dụng chung hệ thống style nhất quán (Design System / Global CSS / UI Component Library chung của dự án). Nghiêm cấm việc mỗi trang tự định nghĩa một kiểu style khác nhau cho cùng một loại phần tử UI.

## Tài liệu chi tiết hơn
- API conventions: docs/api-patterns.md
- Database schema: docs/database-rules.md
- Tiến độ hiện tại: PROGRESS.md
- Quyết định thiết kế: DECISIONS.md