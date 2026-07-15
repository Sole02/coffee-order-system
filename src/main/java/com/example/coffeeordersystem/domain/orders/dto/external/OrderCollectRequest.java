package com.example.coffeeordersystem.domain.orders.dto.external;

import com.example.coffeeordersystem.domain.orders.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCollectRequest {

    private Long orderId;
    private Long userId;
    private Long menuId;
    private String menuName;
    private Long price;
    private OrderStatus status;
    private LocalDateTime occurredAt;
}
