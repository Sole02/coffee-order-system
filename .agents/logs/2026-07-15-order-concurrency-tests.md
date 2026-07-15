# User 낙관적 락 동시성 / 포인트 부족 예외 단위 테스트

- 날짜: 2026-07-15
- 요구사항: (1) 여러 스레드가 동시에 같은 사용자의 포인트를 차감할 때 중복 차감 없이 정확히 처리되는지 검증, (2) 포인트 부족 상태에서 주문 시 `BusinessException(INSUFFICIENT_POINT)`이 발생하는지 검증.

## 생성 파일
- `src/test/java/.../domain/orders/service/OrderServiceTest.java` (신규) — Mockito 순수 단위 테스트. `menuRepository`/`userRepository`를 모킹해 포인트가 부족한 상황을 만들고, `orderService.placeOrderInternal(userId, menuId)` 호출 시 `BusinessException`이 발생하며 `getErrorCode() == INSUFFICIENT_POINT`인지, 그리고 `orderRepository.save(...)`가 호출되지 않는지 검증.
- `src/test/java/.../domain/orders/service/OrderServiceConcurrencyTest.java` (신규) — `@SpringBootTest` 통합 테스트. 전용 User/Menu를 저장한 뒤 `ExecutorService`(스레드 10개) + `CountDownLatch`로 동시에 `orderService.placeOrder(userId, menuId)`를 호출하고, 성공/충돌(`ORDER_CONFLICT`) 건수를 집계해 "차감된 포인트 == 성공 건수 × 가격", "DB에 저장된 주문 수 == 성공 건수"를 검증.

## 구현 중 발견/수정한 문제
- (설계 변경) 낙관적 락 재시도(`MAX_RETRY_COUNT=3`)가 있어도 스레드 10개가 동시에 몰리면 그중 일부가 재시도를 모두 소진하고 `ORDER_CONFLICT`로 실패할 수 있음을 실제 테스트 실행으로 확인. "전부 성공"을 가정하지 않고, 성공/충돌 건수를 각각 집계해 정합성(중복 차감 없음)만 검증하도록 설계를 조정. 프로덕션 코드(`MAX_RETRY_COUNT` 등)는 변경하지 않음.
- (버그 수정) `future.get()` 호출이 try-catch 밖에 있어 첫 `ORDER_CONFLICT` 발생 시 루프가 조기 종료되고 `executorService.shutdown()`이 실행되지 않는 문제 발견 → `future.get()`을 try 블록 안으로 옮기고 `ExecutionException`을 잡아 성공/충돌 건수를 누락 없이 집계하도록 수정.
- (버그 수정) `tearDown()`에서 `userRepository.delete(user)`가 `@BeforeEach` 시점의 오래된(stale, version=0) `user` 인스턴스를 그대로 삭제에 사용해 `ObjectOptimisticLockingFailureException`이 발생하는 문제 발견 → `userRepository.deleteById(user.getId())`로 변경(내부적으로 최신 엔티티를 다시 조회한 뒤 삭제하므로 버전 충돌 없음).

## 검증
- Windows JDK 17 경유(`gradlew.bat test --tests ...OrderServiceTest --tests ...OrderServiceConcurrencyTest`)로 두 테스트 통과 확인.
- 동시성 테스트를 `--rerun`으로 3회 연속 실행해 안정적으로 통과함을 확인.
- `gradlew.bat test`로 전체 테스트 스위트(`CoffeeOrderSystemApplicationTests` 포함) 통과 확인.
