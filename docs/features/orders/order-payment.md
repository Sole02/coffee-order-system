# 주문/결제

## 기능 소개
사용자가 메뉴를 주문하면 즉시 포인트를 차감(결제)하고 주문 내역을 저장한다. 여러 서버·스레드에서 같은 사용자가 동시에 주문하더라도 포인트가 중복 차감되지 않도록 설계되어 있다.

## 엔드포인트
```
POST /orders
Request
{ "userId": 1, "menuId": 3 }

Response 200
{
  "orderId": 100,
  "menuName": "아메리카노",
  "price": 4000,
  "status": "SUCCESS",
  "remainingPoint": 6000
}
```

## 핵심 설계 요약
- `OrderService.placeOrder(userId, menuId)`가 진입점이며, 내부적으로 `@Transactional`이 걸린 `placeOrderInternal(userId, menuId)`을 호출한다. 같은 클래스 안에서 `@Transactional` 메서드를 직접 호출(self-invocation)하면 프록시를 거치지 않아 트랜잭션이 적용되지 않으므로, `@Lazy @Autowired`로 주입한 자기 자신(`self`)을 통해 호출한다.
- `placeOrderInternal`은 메뉴(`MENU_NOT_FOUND`)와 사용자(`USER_NOT_FOUND`)를 조회한 뒤 `User.usePoint(menu.getPrice())`를 호출한다. 포인트가 부족하면 `BusinessException(INSUFFICIENT_POINT)`가 발생하며, 트랜잭션이 롤백되어 주문이 저장되지 않는다.
- 성공하면 `Order(userId, menuId, price, SUCCESS)`를 저장한다. `price`는 주문 시점의 메뉴 가격을 스냅샷으로 저장하므로, 이후 메뉴 가격이 바뀌어도 과거 주문 금액에는 영향이 없다.
- **동시성 제어**: `User.version`(`@Version`) 기반 낙관적 락을 사용한다. 동시에 같은 사용자가 여러 건을 주문해 버전 충돌(`ObjectOptimisticLockingFailureException`)이 나면 `placeOrder`가 최신 데이터를 다시 조회해 최대 3회(`MAX_RETRY_COUNT`)까지 재시도한다. 재시도를 모두 소진하면 `BusinessException(ORDER_CONFLICT, 409)`를 던진다 — 경합이 매우 심하면(예: 10개 스레드 동시 주문) 재시도 3회로도 부족해 일부 요청이 `ORDER_CONFLICT`로 실패할 수 있다(테스트로 확인됨, `OrderServiceConcurrencyTest`).
- `placeOrderAndRespond(userId, menuId)`가 `placeOrder` 성공 후 메뉴/사용자 정보를 다시 조회해 `OrderResponse`를 조립하고, [주문 데이터 수집 전송](../orders/order-data-collection.md)을 트리거한다.

## 관련 파일 목록
- `domain/orders/controller/OrderController.java`
- `domain/orders/service/OrderService.java` (`placeOrder`, `placeOrderInternal`, `placeOrderAndRespond`)
- `domain/orders/dto/request/OrderRequest.java`
- `domain/orders/dto/response/OrderResponse.java`
- `domain/orders/entity/Order.java`, `domain/orders/entity/OrderStatus.java`
- `domain/user/entity/User.java` (`usePoint()`, `version`)
- `global/exception/ErrorCode.java` (`MENU_NOT_FOUND`, `USER_NOT_FOUND`, `INSUFFICIENT_POINT`, `ORDER_CONFLICT`)
