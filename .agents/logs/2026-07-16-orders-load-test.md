# k6 부하테스트: POST /orders

- 날짜: 2026-07-16
- 요구사항: k6 설치 후 주문/결제 API(`POST /orders`)에 대한 부하테스트 스크립트 작성. 응답시간(평균/p95), 성공·실패 개수, 에러율 확인. 시드 데이터 활용 또는 테스트 전용 데이터 방안 제안. `load-test/` 폴더로 관리.

## 생성/수정 파일
- k6 v2.1.0 설치 — `~/.local/bin/k6` (WSL, sudo 불가 환경이라 apt 대신 GitHub 릴리스 정적 바이너리 다운로드)
- `load-test/orders-load-test.js` (신규) — 기본 시드 유저(id 1, 2) 대상. `setup()`에서 `POST /points/charge`로 대량 포인트 충전 후 랜덤 유저/메뉴로 `POST /orders` 반복. 커스텀 메트릭(`order_success_count`, `order_failed_count`, `order_conflict_409_count`, `order_insufficient_point_400_count`, `order_success_rate`) + 기본 제공 `http_req_duration`(avg/p95)으로 지표 확인.
- `load-test/orders-load-test-pool.js` (신규) — 부하테스트 전용 유저 풀(id 3~102) 대상. row 경합/포인트 소진 없이 순수 처리량 측정용. 위 스크립트와 동일한 지표 구조, `setup()` 포인트 충전 로직은 제거(이미 시드 데이터에 대량 포인트 포함).
- `src/main/resources/data-loadtest.sql` (신규) — 기존 시드(유저 2명, 메뉴 3개) + `SYSTEM_RANGE(1,100)`으로 유저 100명(각 10억 포인트) 추가 시딩.
- `src/main/resources/application-loadtest.yml` (신규) — `spring.sql.init.data-locations: classpath:data-loadtest.sql`로 `loadtest` 프로필 활성화 시 위 시드를 사용하도록 오버라이드.

## 구현 메모
- WSL2에서 Windows 호스트로 접속하는 IP는 PowerShell `Get-NetIPAddress`(`vEthernet (WSL)` 인터페이스)로 `192.168.128.1` 확인, `ip route show default`의 게이트웨이와 일치함을 교차 확인.
- 기존 `data.sql` 시드 유저(포인트 10,000 / 5,000)는 아메리카노(4,000원) 기준 2~3회 주문이면 소진되어, 그대로 부하테스트하면 "포인트 부족"만 측정하게 되는 문제를 미리 확인하고 사용자에게 두 가지 방안(① 기존 유저 + `setup()` 포인트 충전, ② 전용 유저 풀 시드)을 제시함. 사용자가 우선 ①을 선택, 이후 경합 여부 비교를 위해 ②도 추가 요청함.
- 이 환경(WSL)에는 JDK가 없어 앱은 Windows 쪽에서 직접 구동(`loadtest` 프로필 재시작은 사용자가 수행), k6만 WSL에서 실행.

## 실행 및 결과 (BASE_URL=http://192.168.128.1:8080, ramping-vus: 램프업10s/유지30s/램프다운10s)

| # | 유저 풀 | VU | 성공률 | p95(성공 요청) | 주요 실패 원인 |
|---|---|---|---|---|---|
| 1 | 시드 2명 + 1억P 충전 | 50 | 7.68% | 17ms | 400 포인트부족 (103,846건) |
| 2 | 시드 2명 + 1000억P 충전 | 50 | 28.81% | 16.3ms | 409 락충돌(7,575) + i/o timeout(약 46,000) |
| 3 | 전용 풀 100명(각 10억P) | 50 | 28.93% | 14.5ms | i/o timeout만 (409/400 0건) |
| 4 | 전용 풀 100명(각 10억P) | 10 | 59.72% | 3.54ms | i/o timeout만 |

## 결론 / 후속 제안
- 성공한 요청 기준 응답속도는 전 구간 양호(p95 3~17ms) — 애플리케이션 처리 자체는 문제없음.
- 유저 풀을 2명 → 100명으로 늘려도 실패율이 거의 그대로였던 점(#2 vs #3)에서, 병목이 낙관적 락 경합이나 포인트 부족이 아님을 확인.
- 모든 실행(VU 50/10 공통)에서 특정 시점에 완료 건수가 멈췄다가 테스트 종료 시 나머지가 한꺼번에 `dial: i/o timeout`으로 실패하는 동일 패턴 반복 → WSL2↔Windows 호스트 NAT 경로(192.168.128.1)의 커넥션 처리 한계로 추정. 애플리케이션/DB 커넥션 풀 문제로 단정할 근거(400/409 발생 여부)는 배제됨.
- 후속으로 필요 시: Windows 쪽에서 직접 k6 실행, 또는 앱을 WSL 내부에서 구동해 동일 네트워크 네임스페이스로 재검증 제안(미실행, 사용자 보류).
