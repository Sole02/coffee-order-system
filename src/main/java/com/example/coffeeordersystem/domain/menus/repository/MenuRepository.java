package com.example.coffeeordersystem.domain.menus.repository;

import com.example.coffeeordersystem.domain.menus.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
