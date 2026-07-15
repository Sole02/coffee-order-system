# 포인트 충전

## 기능 소개
사용자가 보유한 포인트를 충전한다. 충전 전/후 포인트를 함께 응답해 클라이언트가 잔액 변화를 바로 확인할 수 있다.

## 엔드포인트
```
POST /points/charge
Request
{ "userId": 1, "amount": 10000 }

Response 200
{
  "userId": 1,
  "beforePoint": 5000,
  "chargedAmount": 10000,
  "afterPoint": 15000
}
```

## 핵심 설계 요약
- `PointService.chargePoint(userId, amount)`가 `@Transactional`로 동작한다: 사용자를 조회(`USER_NOT_FOUND`)하고, 충전 전 포인트를 기록한 뒤 `User.chargePoint(amount)`(엔티티 내부에서 `point += amount`)를 호출해 상태를 바꾼다.
- 포인트 증감 로직은 `User` 엔티티 안에 캡슐화되어 있고, Service는 필드를 직접 계산하지 않는다.
- 동시 충전 요청에 대한 정합성은 `User.version`(`@Version`) 기반 낙관적 락으로 보장된다. 다만 주문/결제(`OrderService.placeOrder`)와 달리 포인트 충전에는 낙관적 락 충돌 시 재시도 로직이 없다 — 동시에 같은 사용자를 충전하는 요청이 충돌하면 `ObjectOptimisticLockingFailureException`이 그대로 전파된다.
- 충전 금액(`amount`)에 대한 별도 검증(0 이하 금지 등)은 현재 구현되어 있지 않다.

## 관련 파일 목록
- `domain/user/controller/PointController.java`
- `domain/user/service/PointService.java`
- `domain/user/dto/request/PointChargeRequest.java`
- `domain/user/dto/response/PointChargeResponse.java`
- `domain/user/entity/User.java` (`chargePoint()`, `version`)
