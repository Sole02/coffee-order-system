package com.example.coffeeordersystem.domain.orders.service;

import com.example.coffeeordersystem.domain.menus.entity.Menu;
import com.example.coffeeordersystem.domain.menus.repository.MenuRepository;
import com.example.coffeeordersystem.domain.orders.dto.response.OrderResponse;
import com.example.coffeeordersystem.domain.orders.entity.Order;
import com.example.coffeeordersystem.domain.orders.entity.OrderStatus;
import com.example.coffeeordersystem.domain.orders.repository.OrderRepository;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;

    @Lazy
    @Autowired
    private OrderService self;

    private static final int MAX_RETRY_COUNT = 3;

    public Order placeOrder(Long userId, Long menuId) {
        int attempt = 0;

        while (attempt < MAX_RETRY_COUNT) {
            try {
                return self.placeOrderInternal(userId, menuId);
            } catch (ObjectOptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= MAX_RETRY_COUNT) {
                    throw new BusinessException(ErrorCode.ORDER_CONFLICT);
                }
            }
        }

        throw new BusinessException(ErrorCode.ORDER_CONFLICT);
    }

    public OrderResponse placeOrderAndRespond(Long userId, Long menuId) {
        Order order = placeOrder(userId, menuId);

        Menu menu = menuRepository.findById(order.getMenuId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new OrderResponse(
                order.getId(),
                menu.getName(),
                order.getPrice(),
                order.getStatus(),
                user.getPoint()
        );
    }

    @Transactional
    public Order placeOrderInternal(Long userId, Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.usePoint(menu.getPrice());

        Order order = new Order(userId, menuId, menu.getPrice(), OrderStatus.SUCCESS);
        return orderRepository.save(order);
    }
}