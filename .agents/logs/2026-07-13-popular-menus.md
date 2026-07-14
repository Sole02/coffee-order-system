# 인기 메뉴 목록 조회 API

- 날짜: 2026-07-13
- 엔드포인트: `GET /menus/popular`
- 요구사항: 최근 7일간 주문 횟수 기준 상위 3개 메뉴를 rank와 함께 응답

## 생성/수정 파일
- `domain/menus/dto/response/PopularMenuResponse.java` (신규)
- `domain/menus/service/MenuService.java` (수정 — `getPopularMenus` 메서드 추가, `OrderRepository` 의존성 추가)
- `domain/menus/controller/MenuController.java` (수정 — `/menus/popular` 엔드포인트 추가)

## 구현 메모
- `OrderRepository.findPopularMenus(LocalDateTime sevenDaysAgo)`와 `PopularMenuProjection`(menuId, orderCount)은 이미 존재해서 그대로 활용, 수정하지 않음.
- `MenuService`가 `OrderRepository`를 새로 주입받아 상위 3개(`limit(POPULAR_MENU_COUNT)`)만 취하고, 각 menuId로 `MenuRepository`에서 메뉴 이름을 조회해 `rank`(1부터)와 함께 `PopularMenuResponse`로 조립.
- Controller는 새로 만들지 않고 기존 `MenuController`에 엔드포인트만 추가. Repository는 참조하지 않고 `MenuService`만 호출.
- `POPULAR_MENU_COUNT`(3), `POPULAR_MENU_PERIOD_DAYS`(7)는 `OrderService.MAX_RETRY_COUNT`처럼 static final 상수로 정의.
