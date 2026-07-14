package com.example.coffeeordersystem.domain.menus.dto.response;

import lombok.Getter;

@Getter
public class MenuResponse {

    private final Long id;
    private final String name;
    private final Long price;

    public MenuResponse(Long id, String name, Long price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
