# 포인트 충전 API

- 날짜: 2026-07-13
- 엔드포인트: `POST /points/charge`
- 요구사항: 사용자 식별값(userId) + 충전금액(amount)을 받아 포인트 충전

## 생성/수정 파일
- `domain/user/entity/User.java`
- `domain/user/repository/UserRepository.java`
- `domain/user/dto/request/PointChargeRequest.java`
- `domain/user/dto/response/PointChargeResponse.java`
- `domain/user/service/PointService.java`
- `domain/user/controller/PointController.java`

## 구현 메모
- 이 세션에서 새로 만든 게 아니라, 세션 시작 시점에 이미 완성되어 있던 API. AGENTS.md에도 "완료됨"으로 표시되어 있음.
- 이후 만든 모든 API(메뉴 목록, 주문/결제, 인기 메뉴)의 코드 스타일 기준(레퍼런스)으로 계속 참조함: Request DTO는 `@Getter @NoArgsConstructor`, Response DTO는 `@Getter`만 + final 필드/생성자, 예외는 `BusinessException(ErrorCode.xxx)`.
- User의 포인트 동시성 제어는 `@Version` 기반 낙관적 락 사용 (AGENTS.md 컨벤션).
- 이 로그는 `.agents/logs/`가 비어 있던 것을 발견하고 소급 작성함 — 실제 구현 당시의 의사결정 배경은 기록되지 않았음.
