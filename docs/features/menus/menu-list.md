# 커피 메뉴 목록 조회

## 기능 소개
등록된 전체 커피 메뉴 목록을 조회한다. 정렬/페이징/필터링 없이 저장된 모든 메뉴를 그대로 반환하는 단순 조회 기능이다.

## 엔드포인트
```
GET /menus

Response 200
[
  { "id": 1, "name": "아메리카노", "price": 4000 },
  { "id": 2, "name": "카페라떼", "price": 4500 }
]
```

## 핵심 설계 요약
- `MenuService.getMenus()`가 `MenuRepository.findAll()`로 전체 메뉴를 조회한 뒤, 각 `Menu` 엔티티를 `MenuResponse(id, name, price)`로 변환해 반환한다. Entity를 API 응답으로 직접 노출하지 않는다는 프로젝트 컨벤션을 따른다.
- 별도의 페이징/조건 필터가 없어, 메뉴 수가 많아지면 응답 크기가 그대로 커진다(현재 시드 데이터 기준 3종).
- 읽기 전용 트랜잭션(`@Transactional(readOnly = true)`)으로 처리한다.

## 관련 파일 목록
- `domain/menus/controller/MenuController.java`
- `domain/menus/service/MenuService.java` (`getMenus()`)
- `domain/menus/repository/MenuRepository.java`
- `domain/menus/entity/Menu.java`
- `domain/menus/dto/response/MenuResponse.java`
