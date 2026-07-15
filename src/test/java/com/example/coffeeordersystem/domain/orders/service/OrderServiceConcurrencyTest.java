package com.example.coffeeordersystem.domain.orders.service;

import com.example.coffeeordersystem.domain.menus.entity.Menu;
import com.example.coffeeordersystem.domain.menus.repository.MenuRepository;
import com.example.coffeeordersystem.domain.orders.entity.Order;
import com.example.coffeeordersystem.domain.orders.repository.OrderRepository;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderServiceConcurrencyTest {

    private static final int THREAD_COUNT = 10;
    private static final long PRICE = 1000L;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User user;
    private Menu menu;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("동시성테스트유저", PRICE * THREAD_COUNT));
        menu = menuRepository.save(new Menu("동시성테스트메뉴", PRICE));
    }

    @AfterEach
    void tearDown() {
        orderRepository.findAll().stream()
                .filter(order -> order.getUserId().equals(user.getId()))
                .forEach(orderRepository::delete);
        userRepository.deleteById(user.getId());
        menuRepository.delete(menu);
    }

    @Test
    void 동시에_여러_스레드가_주문해도_포인트가_중복_차감되지_않는다() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Order>> futures = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            futures.add(executorService.submit(() -> {
                startLatch.await();
                return orderService.placeOrder(user.getId(), menu.getId());
            }));
        }
        startLatch.countDown();

        int successCount = 0;
        int conflictCount = 0;
        for (Future<Order> future : futures) {
            try {
                future.get(10, TimeUnit.SECONDS);
                successCount++;
            } catch (ExecutionException e) {
                // 재시도(MAX_RETRY_COUNT)를 모두 소진했을 때만 발생하는 정상적인 실패 경로.
                assertThat(e.getCause()).isInstanceOf(BusinessException.class);
                assertThat(((BusinessException) e.getCause()).getErrorCode()).isEqualTo(ErrorCode.ORDER_CONFLICT);
                conflictCount++;
            }
        }
        executorService.shutdown();

        User finalUser = userRepository.findById(user.getId()).orElseThrow();
        long orderCountInDb = orderRepository.findAll().stream()
                .filter(order -> order.getUserId().equals(user.getId()))
                .count();

        assertThat(successCount + conflictCount).isEqualTo(THREAD_COUNT);
        assertThat(finalUser.getPoint()).isEqualTo(PRICE * (THREAD_COUNT - successCount));
        assertThat(orderCountInDb).isEqualTo(successCount);
    }
}
