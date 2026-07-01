package com.example.back_end.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1003, "User not found", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(1004, "Invalid password", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1005, "Unauthorized access", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1006, "Forbidden access", HttpStatus.FORBIDDEN),
    FILE_UPLOAD_ERROR(1007, "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    RESOURCE_NOT_FOUND(1008, "Resource not found", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
