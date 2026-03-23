package com.autonix.simulator_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 커스텀 비즈니스 예외
    @ExceptionHandler(SimulationException.class)
    public ResponseEntity<String> handleSimulationException(SimulationException e) {
        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }

    // 2. Feign 호출 실패 (Line-Service 등 외부 서비스 연결 오류)
    @ExceptionHandler(feign.FeignException.class)
    public ResponseEntity<String> handleFeignException(feign.FeignException e) {
        return ResponseEntity.status(503).body("외부 서비스 호출 실패: " + e.getMessage());
    }

    // 3. 잘못된 요청 파라미터
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // 4. 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다: " + e.getMessage());
    }
}