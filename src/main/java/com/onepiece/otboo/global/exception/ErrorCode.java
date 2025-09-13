package com.onepiece.otboo.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // COMMON
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러입니다.", "관리자에게 연락해 주세요."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", "잘못된 요청을 진행하였습니다."),
    AUTHORIZATION_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다.", ""),

    // USER
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "사용자 등록 실패", "사용자가 이미 존재합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 확인 실패", "존재하지 않는 사용자입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 유효하지 않습니다.", "비밀번호를 확인해 주세요.");

    private final HttpStatus status;
    private final String message;
    private final String detail;

    ErrorCode(HttpStatus status, String message, String detail) {
        this.status = status;
        this.message = message;
        this.detail = detail;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getDetail() {
        return detail;
    }
}
