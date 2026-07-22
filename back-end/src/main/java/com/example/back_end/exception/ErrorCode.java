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
    USER_BLOCKED(1009, "Tài khoản đã bị khóa. Vui lòng liên hệ giáo viên hoặc quản trị viên.", HttpStatus.FORBIDDEN),
    EMAIL_REQUIRED(1010, "Email không được để trống", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1011, "Địa chỉ email không hợp lệ", HttpStatus.BAD_REQUEST),
    USERNAME_REQUIRED(1012, "Tên tài khoản không được để trống", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID_SIZE(1013, "Tên tài khoản phải từ 3 đến 50 ký tự", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1014, "Mật khẩu không được để trống", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_SHORT(1015, "Mật khẩu phải từ 6 ký tự trở lên", HttpStatus.BAD_REQUEST),
    NAME_REQUIRED(1016, "Tên đầy đủ không được để trống", HttpStatus.BAD_REQUEST),
    FRONT_TEXT_REQUIRED(1017, "Mặt trước của thẻ không được để trống", HttpStatus.BAD_REQUEST),
    BACK_TEXT_REQUIRED(1018, "Mặt sau của thẻ không được để trống", HttpStatus.BAD_REQUEST),
    TOPIK_LEVEL_REQUIRED(1019, "Cấp độ TOPIK không được để trống", HttpStatus.BAD_REQUEST),
    COURSE_TITLE_REQUIRED(1020, "Tiêu đề khóa học không được để trống.", HttpStatus.BAD_REQUEST),
    COURSE_LEVEL_REQUIRED(1021, "Cấp độ khóa học không được để trống.", HttpStatus.BAD_REQUEST),
    TOTAL_SESSIONS_REQUIRED(1022, "Số buổi học không được để trống.", HttpStatus.BAD_REQUEST),
    GRADE_LIST_EMPTY(1023, "Danh sách chấm điểm không được rỗng", HttpStatus.BAD_REQUEST),
    ANSWER_ID_REQUIRED(1024, "Mã câu trả lời không được để trống", HttpStatus.BAD_REQUEST),
    POINTS_EARNED_REQUIRED(1025, "Điểm số không được để trống", HttpStatus.BAD_REQUEST),
    POINTS_EARNED_MIN(1026, "Điểm đạt được không được âm", HttpStatus.BAD_REQUEST),
    QUESTION_TEXT_REQUIRED(1027, "Nội dung câu hỏi không được để trống", HttpStatus.BAD_REQUEST),
    QUESTION_TYPE_REQUIRED(1028, "Loại câu hỏi không được để trống", HttpStatus.BAD_REQUEST),
    QUESTION_POINTS_REQUIRED(1029, "Điểm số không được để trống", HttpStatus.BAD_REQUEST),
    QUESTION_POINTS_MIN(1030, "Điểm số không được âm", HttpStatus.BAD_REQUEST),
    SECTION_REQUIRED(1031, "Phần thi không được để trống", HttpStatus.BAD_REQUEST),
    QUIZ_TITLE_REQUIRED(1032, "Tiêu đề bài kiểm tra không được để trống", HttpStatus.BAD_REQUEST),
    TIME_LIMIT_REQUIRED(1033, "Thời gian làm bài không được để trống", HttpStatus.BAD_REQUEST),
    TIME_LIMIT_MIN(1034, "Thời gian làm bài tối thiểu 1 phút", HttpStatus.BAD_REQUEST),
    TOTAL_SCORE_REQUIRED(1035, "Tổng điểm không được để trống", HttpStatus.BAD_REQUEST),
    TOTAL_SCORE_MIN(1036, "Tổng điểm phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    DUE_DATE_REQUIRED(1037, "Hạn nộp bài không được để trống", HttpStatus.BAD_REQUEST),
    SUBMIT_ANSWERS_EMPTY(1038, "Danh sách câu trả lời không được rỗng", HttpStatus.BAD_REQUEST),
    QUESTION_ID_REQUIRED(1039, "Mã câu hỏi không được để trống", HttpStatus.BAD_REQUEST),
    TIME_TAKEN_REQUIRED(1040, "Thời gian làm bài không được để trống", HttpStatus.BAD_REQUEST),
    TIME_TAKEN_MIN(1041, "Thời gian làm bài không được âm", HttpStatus.BAD_REQUEST),
    CLASS_ID_REQUIRED(1042, "Class ID không được để trống.", HttpStatus.BAD_REQUEST),
    SESSION_NUMBER_REQUIRED(1043, "Số thứ tự buổi học không được để trống.", HttpStatus.BAD_REQUEST),
    STUDY_SET_TITLE_REQUIRED(1044, "Tiêu đề học phần không được để trống", HttpStatus.BAD_REQUEST),
    TOPIK_NAME_REQUIRED(1045, "Tên cấp độ không được để trống", HttpStatus.BAD_REQUEST),
    TOPIK_CODE_REQUIRED(1046, "Mã cấp độ không được để trống", HttpStatus.BAD_REQUEST),
    TOPIK_GROUP_REQUIRED(1047, "Nhóm cấp độ không được để trống", HttpStatus.BAD_REQUEST),
    FULL_NAME_REQUIRED(1048, "Tên đầy đủ không được để trống", HttpStatus.BAD_REQUEST),
    SESSION_HIJACKED(1049, "Tài khoản đã đăng nhập trên thiết bị khác. Phiên hiện tại đã bị kết thúc.", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED_SHARING(1050, "Tài khoản bị tạm khóa do đăng nhập từ quá nhiều vị trí trong ngày. Vui lòng liên hệ giáo viên để mở khóa.", HttpStatus.FORBIDDEN),
    INVALID_SECTION(1051, "Phần thi không phù hợp với loại đề thi này.", HttpStatus.BAD_REQUEST),
    GOOGLE_ACCOUNT_REQUIRED(1052, "Tài khoản này được đăng ký qua Google. Vui lòng đăng nhập bằng Google.", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
