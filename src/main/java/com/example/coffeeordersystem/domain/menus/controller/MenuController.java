package com.example.coffeeordersystem.domain.menus.controller;

import com.example.coffeeordersystem.domain.menus.dto.response.MenuResponse;
import com.example.coffeeordersystem.domain.menus.dto.response.PopularMenuResponse;
import com.example.coffeeordersystem.domain.menus.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/menus")
    public List<MenuResponse> getMenus() {
        return menuService.getMenus();
    }

    @GetMapping("/menus/popular")
    public List<PopularMenuResponse> getPopularMenus() {
        return menuService.getPopularMenus();
    }
}
