# 커피 주문/결제 API

- 날짜: 2026-07-13
- 엔드포인트: `POST /orders`
- 요구사항: 사용자ID(userId), 메뉴ID(menuId)를 받아 주문/결제 처리. 응답에 orderId, menuName, price, status, remainingPoint 포함.

## 생성/수정 파일
- `domain/orders/dto/request/OrderRequest.java` (신규)
- `domain/orders/dto/response/OrderResponse.java` (신규)
- `domain/orders/service/OrderService.java` (수정 — `placeOrderAndRespond` 메서드 추가)
- `domain/orders/controller/OrderController.java` (신규)

## 구현 메모
- `OrderService.placeOrder(userId, menuId)`(재시도 로직 포함, 낙관적 락 충돌 시 `ORDER_CONFLICT`)는 이미 완성되어 있던 로직이라 수정하지 않음. `Order` 엔티티만 반환하고 `menuName`/`remainingPoint`는 없음.
- 처음엔 Controller가 `MenuRepository`/`UserRepository`를 직접 참조해 응답을 조립하는 방식으로 계획했으나, Controller-Service-Repository 계층 규칙 위반이라는 피드백을 받고 수정.
- 최종적으로 `OrderService`에 `placeOrderAndRespond(userId, menuId)`를 추가: 기존 `placeOrder` 호출 → `MenuRepository`/`UserRepository`로 메뉴 이름/잔여 포인트 조회 → `OrderResponse` 조립. `OrderService`에 두 리포지토리가 이미 주입돼 있어 추가 의존성 없이 재사용.
- Controller는 이 Service 메서드 하나만 호출.
