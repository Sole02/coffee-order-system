package com.example.coffeeordersystem.domain.orders.service;

import com.example.coffeeordersystem.domain.menus.entity.Menu;
import com.example.coffeeordersystem.domain.menus.repository.MenuRepository;
import com.example.coffeeordersystem.domain.orders.client.OrderDataCollectionClient;
import com.example.coffeeordersystem.domain.orders.repository.OrderRepository;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDataCollectionClient orderDataCollectionClient;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(userRepository, menuRepository, orderRepository, orderDataCollectionClient);
    }

    @Test
    void 포인트가_부족하면_주문_시_BusinessException이_발생한다() {
        Long userId = 1L;
        Long menuId = 1L;
        Menu menu = new Menu("바닐라라떼", 5000L);
        User user = new User("재석", 1000L);

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> orderService.placeOrderInternal(userId, menuId))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.INSUFFICIENT_POINT));

        verify(orderRepository, never()).save(any());
    }
}
