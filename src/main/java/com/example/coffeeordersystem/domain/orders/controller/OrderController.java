package com.example.coffeeordersystem.domain.orders.controller;

import com.example.coffeeordersystem.domain.orders.dto.request.OrderRequest;
import com.example.coffeeordersystem.domain.orders.dto.response.OrderResponse;
import com.example.coffeeordersystem.domain.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public OrderResponse placeOrder(@RequestBody OrderRequest request) {
        return orderService.placeOrderAndRespond(request.getUserId(), request.getMenuId());
    }
}
