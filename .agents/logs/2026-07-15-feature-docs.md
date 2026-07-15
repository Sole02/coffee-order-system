# AGENTS.md 문서화 단계 추가 + 기존 기능 5종 소급 문서화

- 날짜: 2026-07-15
- 요구사항: `AGENTS.md` 작업 절차에 "문서화"(7번) 단계와 문서 경로를 추가하고, 이미 만든 기능 5개에 대해 `docs/features/{도메인}/{기능}.md` 형식으로 소급 문서화.

## 생성/수정 파일
- `AGENTS.md` — 확인해보니 7번 문서화 단계와 "문서 경로 → docs/features/{도메인}/{기능}.md" 항목이 이미 반영되어 있었음(별도 편집 불필요).
- `docs/features/menus/menu-list.md` (신규) — 메뉴 목록 조회
- `docs/features/menus/popular-menus.md` (신규) — 인기 메뉴 조회
- `docs/features/user/point-charge.md` (신규) — 포인트 충전
- `docs/features/orders/order-payment.md` (신규) — 주문/결제
- `docs/features/orders/order-data-collection.md` (신규) — 주문 데이터 수집 플랫폼 비동기 전송

## 구현 메모
- `.agents/logs/`(그날의 작업 일지, 과거형)와 `docs/features/`(기능이 지금 어떻게 동작하는지 설명하는 상시 최신 문서, 현재형)의 성격 차이를 반영해, 문서는 전부 현재형으로 서술.
- 각 문서는 기능 소개 / 엔드포인트 / 핵심 설계 요약 / 관련 파일 목록 4개 섹션으로 통일.
- 핵심 설계 요약에는 이번 세션에서 실제 테스트로 확인한 사실(예: 주문 동시성 경합이 심하면 재시도 3회로도 `ORDER_CONFLICT`가 날 수 있음, 포인트 충전에는 주문과 달리 낙관적 락 재시도 로직이 없음)도 정확히 반영.
- 코드 변경 없음.

## 검증
- 문서에 적은 상수값(`MAX_RETRY_COUNT=3`, `POPULAR_MENU_COUNT=3`, `POPULAR_MENU_PERIOD_DAYS=7`, `dataCollectionExecutor` core=2/max=5/queue=100, `data-collection.base-url`)을 실제 소스와 grep으로 재대조해 일치 확인.
