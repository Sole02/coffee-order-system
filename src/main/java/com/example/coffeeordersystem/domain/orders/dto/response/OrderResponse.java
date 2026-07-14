package com.example.coffeeordersystem.domain.orders.dto.response;

import com.example.coffeeordersystem.domain.orders.entity.OrderStatus;
import lombok.Getter;

@Getter
public class OrderResponse {

    private final Long orderId;
    private final String menuName;
    private final Long price;
    private final OrderStatus status;
    private final Long remainingPoint;

    public OrderResponse(Long orderId, String menuName, Long price, OrderStatus status, Long remainingPoint) {
        this.orderId = orderId;
        this.menuName = menuName;
        this.price = price;
        this.status = status;
        this.remainingPoint = remainingPoint;
    }
}
