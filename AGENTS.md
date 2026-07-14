# AGENTS.md

## 프로젝트
Spring Boot 기반 커피숍 주문 시스템 (다수 서버 환경 대응)

## 작업 절차
1. **요청 유형 판단**: 이 요청이 코드 구현인지, 문서 작성인지, 기존 코드 검토인지 파악한다.
2. **분석**: 코드 구현 요청인 경우, 기존 코드(Entity, Repository, Service)에 이미 있는지 확인하고, 재사용할지 새로 만들지 판단한다.
3. **계획 수립**: 어떤 파일을 만들지/수정할지 계획을 세운다.
   아래 "코드 컨벤션"을 반드시 따른다.
   계획을 먼저 제시하고 승인받은 뒤 진행한다. 승인 없이 코드를 작성하지 않는다.
4. **구현**: 승인된 계획대로 작업한다.
5. **검증**: 컴파일 확인을 시도한다. 실패 시 원인과 함께 사용자에게 보고한다.
6. **기록**: 만든 것과 수정한 파일을 정리하여 .agents/logs/ 에 남긴다.

## 문서 경로
- 상세 컨벤션 → .agents/docs/convention.md

## 패키지 구조
도메인별 패키지 구조를 사용한다: domain/{도메인명}/{controller, service, repository, entity, dto}
dto는 다시 request/response 하위 폴더로 나눈다.

## 참고할 기존 코드
domain/user 패키지 (PointController, PointService, PointChargeRequest/Response)가 완성된 예시. 새 API 만들 때 이 스타일을 그대로 따른다.

## 주의사항
- git commit, git push는 명시적 요청이 있을 때만 실행한다.