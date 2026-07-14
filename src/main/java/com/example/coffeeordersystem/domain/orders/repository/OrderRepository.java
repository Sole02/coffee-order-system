package com.example.coffeeordersystem.domain.orders.repository;

import com.example.coffeeordersystem.domain.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o.menuId as menuId, COUNT(o) as orderCount " +
            "FROM Order o " +
            "WHERE o.createdAt >= :sevenDaysAgo AND o.status = 'SUCCESS' " +
            "GROUP BY o.menuId " +
            "ORDER BY COUNT(o) DESC")
    List<PopularMenuProjection> findPopularMenus(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);
}