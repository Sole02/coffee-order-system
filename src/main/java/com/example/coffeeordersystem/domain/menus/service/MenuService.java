package com.example.coffeeordersystem.domain.menus.service;

import com.example.coffeeordersystem.domain.menus.dto.response.MenuResponse;
import com.example.coffeeordersystem.domain.menus.dto.response.PopularMenuResponse;
import com.example.coffeeordersystem.domain.menus.entity.Menu;
import com.example.coffeeordersystem.domain.menus.repository.MenuRepository;
import com.example.coffeeordersystem.domain.orders.repository.OrderRepository;
import com.example.coffeeordersystem.domain.orders.repository.PopularMenuProjection;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;

    private static final int POPULAR_MENU_COUNT = 3;
    private static final int POPULAR_MENU_PERIOD_DAYS = 7;

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenus() {
        return menuRepository.findAll().stream()
                .map(menu -> new MenuResponse(menu.getId(), menu.getName(), menu.getPrice()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PopularMenuResponse> getPopularMenus() {
        List<PopularMenuProjection> popularMenus = orderRepository
                .findPopularMenus(LocalDateTime.now().minusDays(POPULAR_MENU_PERIOD_DAYS))
                .stream()
                .limit(POPULAR_MENU_COUNT)
                .toList();

        return IntStream.range(0, popularMenus.size())
                .mapToObj(i -> {
                    PopularMenuProjection projection = popularMenus.get(i);
                    Menu menu = menuRepository.findById(projection.getMenuId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

                    return new PopularMenuResponse(i + 1, menu.getId(), menu.getName(), projection.getOrderCount());
                })
                .toList();
    }
}
