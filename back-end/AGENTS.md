

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
4. Đặt tên API theo chuẩn: /api/v1/{resource}
5. Mọi lỗi phải trả về qua GlobalExceptionHandler
6. Tên biến, method dùng tiếng Anh — comment có thể tiếng Việt
7. Viết test cho mọi Service method quan trọng

## Tài liệu chi tiết hơn
- API conventions: docs/api-patterns.md
- Database schema: docs/database-rules.md
- Tiến độ hiện tại: PROGRESS.md
- Quyết định thiết kế: DECISIONS.md