package com.example.back_end.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Từ khóa lỗi không hợp lệ", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "Tài khoản hoặc địa chỉ email đã tồn tại trên hệ thống", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1003, "Tài khoản không tồn tại trên hệ thống", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(1004, "Mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1005, "Truy cập không được ủy quyền hoặc phiên đăng nhập đã hết hạn", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1006, "Bạn không có quyền thực hiện thao tác này", HttpStatus.FORBIDDEN),
    FILE_UPLOAD_ERROR(1007, "Tải tệp tin lên hệ thống thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    RESOURCE_NOT_FOUND(1008, "Không tìm thấy tài nguyên được yêu cầu", HttpStatus.NOT_FOUND),
    USER_BLOCKED(1009, "Tài khoản đã bị khóa. Vui lòng liên hệ giáo viên hoặc quản trị viên.", HttpStatus.FORBIDDEN);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
