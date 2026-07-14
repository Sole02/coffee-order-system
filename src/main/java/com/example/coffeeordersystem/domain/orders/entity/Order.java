package com.example.coffeeordersystem.domain.orders.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long menuId;

    private Long price;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;

    public Order(Long userId, Long menuId, Long price, OrderStatus status) {
        this.userId = userId;
        this.menuId = menuId;
        this.price = price;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
}