# 주문 데이터 수집 플랫폼 전송

## 기능 소개
[주문/결제](./order-payment.md)가 성공하면 주문 내역을 외부 데이터 수집 플랫폼으로 실시간 전송한다. 실제 외부 플랫폼이 없으므로, 프로젝트 안에 이를 흉내내는 가짜(mock) 수집 서버 엔드포인트를 두고 그쪽으로 비동기 HTTP 요청을 보낸다. 전송이 느려지거나 실패해도 주문/결제 응답에는 영향을 주지 않는 fire-and-forget 방식이다.

## 엔드포인트
```
POST /mock/data-platform/orders   (가짜 수집 서버, 프로젝트 내부용)
Request
{
  "orderId": 100,
  "userId": 1,
  "menuId": 3,
  "menuName": "아메리카노",
  "price": 4000,
  "status": "SUCCESS",
  "occurredAt": "2026-07-15T10:29:22.886842300"
}

Response 200 (본문 없음)
```
사용자가 직접 호출하는 API가 아니라, `OrderService.placeOrderAndRespond()`가 주문 성공 시 내부적으로만 호출한다.

## 핵심 설계 요약
- `OrderService.placeOrderAndRespond()`에서 주문이 확정된 뒤(포인트 차감 트랜잭션이 이미 커밋된 시점) `OrderDataCollectionClient.sendOrderData(OrderCollectRequest)`를 호출한다.
- `OrderDataCollectionClient.sendOrderData`는 `@Async("dataCollectionExecutor")`로 별도 스레드에서 실행되며, `RestClient`로 `{data-collection.base-url}/mock/data-platform/orders`에 POST한다. `RestClientException`이 발생해도 로그만 남기고 예외를 다시 던지지 않는다 — 주문 흐름과 완전히 분리되어 있다.
- 비동기 실행에 필요한 `@EnableAsync`와 전용 스레드풀(`dataCollectionExecutor`, core 2 / max 5 / queue 100)은 `AsyncConfig`에서 설정한다.
- 가짜 수집 서버(`DataCollectionMockController`)는 받은 데이터를 로그로 남기고 200을 반환하기만 한다. 실제 도메인이 아니라 외부 시스템을 흉내내는 코드임을 명확히 하기 위해 `domain` 패키지가 아닌 별도의 최상위 `mock` 패키지에 위치한다.
- 전송 base-url은 `application.yml`의 `data-collection.base-url`로 설정하며, 현재는 같은 애플리케이션(자기 자신)의 mock 엔드포인트를 가리킨다(`http://localhost:8080`).

## 관련 파일 목록
- `domain/orders/client/OrderDataCollectionClient.java`
- `domain/orders/dto/external/OrderCollectRequest.java`
- `global/config/AsyncConfig.java`
- `mock/controller/DataCollectionMockController.java`
- `domain/orders/service/OrderService.java` (`placeOrderAndRespond()` 내 호출부)
- `src/main/resources/application.yml` (`data-collection.base-url`)
