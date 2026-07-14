package com.example.coffeeordersystem.global.exception;

public enum ErrorCode {

    // User
    USER_NOT_FOUND("존재하지 않는 사용자입니다."),
    INSUFFICIENT_POINT("포인트가 부족합니다."),

    // Menu
    MENU_NOT_FOUND("존재하지 않는 메뉴입니다."),

    // Order
    ORDER_CONFLICT("일시적으로 주문이 몰려 처리에 실패했습니다. 다시 시도해주세요.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}