# AGENTS.md

## 프로젝트
Spring Boot 기반 커피숍 주문 시스템 (다수 서버 환경 대응)

## 패키지 구조
도메인별 패키지 구조를 사용한다: domain/{도메인명}/{controller, service, repository, entity, dto}
dto는 다시 request/response 하위 폴더로 나눈다.

## 문서 경로
- 상세 컨벤션 → .agents/docs/convention.md
- 작업 기록 → .agents/logs/

## 코드 컨벤션
- Entity를 Controller에 직접 노출하지 않는다. DTO로 변환해서 응답한다.
- Request DTO: @Getter, @NoArgsConstructor 사용 (Setter 없음)
- Response DTO: @Getter만 사용, final 필드 + 생성자로 값을 채워서 생성 (Setter 없음)
- 비즈니스 예외는 BusinessException + ErrorCode(enum)로 처리한다. Java 기본 예외(IllegalArgumentException 등) 사용하지 않는다.
- Entity 안에서 자기 자신의 상태를 변경하는 로직(예: chargePoint, usePoint)을 캡슐화한다. Service에서 직접 필드를 계산하지 않는다.
- User의 point 동시성 제어는 낙관적 락(@Version)을 사용한다.

## 참고할 기존 코드
domain/user 패키지 (PointController, PointService, PointChargeRequest/Response)가 완성된 예시. 새 API 만들 때 이 스타일을 그대로 따른다.