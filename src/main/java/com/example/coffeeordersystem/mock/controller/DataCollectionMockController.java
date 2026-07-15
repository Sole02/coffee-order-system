package com.example.coffeeordersystem.mock.controller;

import com.example.coffeeordersystem.domain.orders.dto.external.OrderCollectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataCollectionMockController {

    private static final Logger log = LoggerFactory.getLogger(DataCollectionMockController.class);

    @PostMapping("/mock/data-platform/orders")
    public void receiveOrder(@RequestBody OrderCollectRequest request) {
        log.info("[데이터 수집 플랫폼] 주문 수신: orderId={}, userId={}, menuName={}, price={}, status={}, occurredAt={}",
                request.getOrderId(), request.getUserId(), request.getMenuName(),
                request.getPrice(), request.getStatus(), request.getOccurredAt());
    }
}
