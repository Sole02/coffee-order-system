# 주문 내역 데이터 수집 플랫폼 비동기 전송

- 날짜: 2026-07-15
- 요구사항: 주문/결제 API에서 주문 성공 시, 주문 내역을 데이터 수집 플랫폼으로 실시간(비동기) 전송. 실제 플랫폼이 없으므로 프로젝트 안에 가짜 수집 서버 엔드포인트를 만들고 비동기 HTTP 요청으로 전송.

## 생성/수정 파일
- `global/config/AsyncConfig.java` (신규) — `@EnableAsync`, `dataCollectionExecutor` 스레드풀 빈(core 2 / max 5 / queue 100).
- `domain/orders/dto/external/OrderCollectRequest.java` (신규) — 전송 payload. `orderId, userId, menuId, menuName, price, status, occurredAt`.
- `domain/orders/client/OrderDataCollectionClient.java` (신규) — `@Async("dataCollectionExecutor")`로 `RestClient`를 통해 mock 엔드포인트에 POST. 전송 실패 시 예외를 던지지 않고 로그만 남김(주문 흐름에 영향 없음).
- `mock/controller/DataCollectionMockController.java` (신규) — `POST /mock/data-platform/orders`, 수신 내용을 로그로 출력.
- `domain/orders/service/OrderService.java` (수정) — `placeOrderAndRespond()`에서 `Order`/`Menu`/`User` 조회 후, 응답 조립 직전에 `orderDataCollectionClient.sendOrderData(...)` 호출.
- `src/main/resources/application.yml` (수정) — `data-collection.base-url: http://localhost:8080` 추가.

## 구현 메모
- 훅 지점은 `placeOrder()`(재시도+낙관적 락 로직)가 예외 없이 리턴한 뒤, 즉 주문/포인트차감 트랜잭션이 이미 커밋된 시점으로 선택. 수집 전송 실패가 주문 성공 여부에 영향을 주지 않도록 함.
- `OrderCollectRequest`는 우리 쪽에서 생성해 보내는 payload인 동시에 mock 컨트롤러가 `@RequestBody`로 역직렬화하는 대상이라, 기존 Request/Response DTO 컨벤션을 절충해 `@Getter @NoArgsConstructor @AllArgsConstructor`로 구성.
- `OrderDataCollectionClient`는 `OrderService`와 별개의 빈이라 `@Async` self-invocation 문제가 없음 (기존 `OrderService`의 `@Lazy @Autowired self` 패턴과는 무관, 그대로 재사용 안 함).
- 가짜 수집 서버는 실제 도메인이 아님을 명확히 하기 위해 `domain` 밖의 별도 최상위 패키지 `com.example.coffeeordersystem.mock`에 위치.

## 검증
- Windows 쪽 JDK 17(`C:\Program Files\Java\jdk-17`)로 `gradlew.bat compileJava` 컴파일 성공 확인 (WSL 환경에 JDK가 없어 cmd.exe 경유).
- `gradlew.bat bootRun`으로 앱 실행 후 `POST /orders {"userId":1,"menuId":1}` 호출 → `200 OK`, `remainingPoint` 정상 차감 확인.
- 애플리케이션 로그에서 `DataCollectionMockController`가 동일 주문 데이터(`orderId=1, userId=1, menuName=아메리카노, price=4000, status=SUCCESS`)를 수신·로깅한 것을 확인. `/orders` 응답이 먼저 반환된 뒤 별도로 로그가 찍혀 비동기 동작을 확인.
