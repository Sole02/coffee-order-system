package com.example.coffeeordersystem.domain.orders.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderRequest {

    private Long userId;
    private Long menuId;
}
