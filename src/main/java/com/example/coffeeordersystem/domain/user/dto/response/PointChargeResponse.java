package com.example.coffeeordersystem.domain.user.dto.response;

import lombok.Getter;

@Getter
public class PointChargeResponse {

    private final Long userId;
    private final Long beforePoint;
    private final Long chargedAmount;
    private final Long afterPoint;

    public PointChargeResponse(Long userId, Long beforePoint, Long chargedAmount, Long afterPoint) {
        this.userId = userId;
        this.beforePoint = beforePoint;
        this.chargedAmount = chargedAmount;
        this.afterPoint = afterPoint;
    }
}