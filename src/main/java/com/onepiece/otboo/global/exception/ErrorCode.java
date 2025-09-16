package com.onepiece.otboo.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // COMMON
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러입니다.", "관리자에게 연락해 주세요."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", "잘못된 요청을 진행하였습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청 경로를 찾을 수 없음", "존재하지 않는 경로입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드", "지원하지 않는 메서드입니다."),

    // USER
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "사용자 등록 실패", "사용자가 이미 존재합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 확인 실패", "존재하지 않는 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일", "이미 존재하는 이메일입니다."),

    // AUTH
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.", "아이디 또는 비밀번호를 확인해 주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", "이 리소스에 접근할 권한이 없습니다."),
    TOKEN_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 생성에 실패했습니다.", "다시 로그인해 주세요."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다.", "다시 로그인해 주세요."),
    TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "토큰 처리 중 에러가 발생했습니다.", "다시 로그인해 주세요."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.", "다시 로그인해 주세요."),
    ;

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
