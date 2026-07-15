# 인기 메뉴 조회

## 기능 소개
최근 7일간 결제가 완료된 주문을 기준으로 가장 많이 팔린 메뉴 상위 3개를 순위와 함께 조회한다.

## 엔드포인트
```
GET /menus/popular

Response 200
[
  { "rank": 1, "menuId": 3, "menuName": "아메리카노", "orderCount": 152 },
  { "rank": 2, "menuId": 1, "menuName": "카페라떼", "orderCount": 98 }
]
```

## 핵심 설계 요약
- `MenuService.getPopularMenus()`가 `OrderRepository.findPopularMenus(sevenDaysAgo)`를 호출한다. 이 쿼리는 `orders.created_at >= :sevenDaysAgo AND orders.status = 'SUCCESS'` 조건으로 `menu_id`별 주문 건수를 `COUNT(o) DESC`로 집계한다.
- 상태가 `SUCCESS`인 주문만 집계 대상이다. 실패한 주문은 애초에 저장되지 않으므로(주문/결제 문서 참고) 자연스럽게 집계에서 제외된다.
- 집계 결과 중 상위 `POPULAR_MENU_COUNT`(3)개만 잘라, 순서대로 1부터 순위(`rank`)를 매긴다.
- 각 집계 결과의 `menuId`로 `MenuRepository.findById`를 다시 조회해 메뉴 이름을 채운다. 조회에 실패하면 `MENU_NOT_FOUND`.
- 읽기 전용 트랜잭션(`@Transactional(readOnly = true)`)으로 처리한다.

## 관련 파일 목록
- `domain/menus/controller/MenuController.java`
- `domain/menus/service/MenuService.java` (`getPopularMenus()`, `POPULAR_MENU_COUNT`, `POPULAR_MENU_PERIOD_DAYS`)
- `domain/orders/repository/OrderRepository.java` (`findPopularMenus`)
- `domain/orders/repository/PopularMenuProjection.java`
- `domain/menus/dto/response/PopularMenuResponse.java`
