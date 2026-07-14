package com.example.coffeeordersystem.domain.user.service;

import com.example.coffeeordersystem.domain.user.dto.response.PointChargeResponse;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserRepository userRepository;

    @Transactional
    public PointChargeResponse chargePoint(Long userId, Long amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Long beforePoint = user.getPoint();

        user.chargePoint(amount);

        return new PointChargeResponse(
                user.getId(),
                beforePoint,
                amount,
                user.getPoint()
        );
    }
}