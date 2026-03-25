package com.dreamlog.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "리프레시 토큰을 찾을 수 없습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // Diary
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "일기를 찾을 수 없습니다."),
    DIARY_NOT_EDITABLE(HttpStatus.FORBIDDEN, "수정 가능 시간이 지났습니다."),
    DIARY_LOCKED(HttpStatus.FORBIDDEN, "아직 잠금 해제 날짜가 도래하지 않았습니다."),

    // Challenge
    CHALLENGE_NOT_FOUND(HttpStatus.NOT_FOUND, "챌린지를 찾을 수 없습니다."),
    CHALLENGE_NOT_JOINABLE(HttpStatus.BAD_REQUEST, "참가할 수 없는 챌린지입니다."),
    CHALLENGE_ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참가 중인 챌린지입니다."),
    CHALLENGE_FULL(HttpStatus.BAD_REQUEST, "참가 인원이 가득 찼습니다."),
    CHALLENGE_NOT_JOINED(HttpStatus.NOT_FOUND, "참가하지 않은 챌린지입니다."),

    // Common
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
