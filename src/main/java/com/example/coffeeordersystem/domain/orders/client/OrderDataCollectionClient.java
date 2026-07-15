package com.example.coffeeordersystem.domain.orders.client;

import com.example.coffeeordersystem.domain.orders.dto.external.OrderCollectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OrderDataCollectionClient {

    private static final Logger log = LoggerFactory.getLogger(OrderDataCollectionClient.class);

    private final RestClient restClient;

    public OrderDataCollectionClient(@Value("${data-collection.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Async("dataCollectionExecutor")
    public void sendOrderData(OrderCollectRequest request) {
        try {
            restClient.post()
                    .uri("/mock/data-platform/orders")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("주문 데이터 수집 플랫폼 전송 실패: orderId={}", request.getOrderId(), e);
        }
    }
}
