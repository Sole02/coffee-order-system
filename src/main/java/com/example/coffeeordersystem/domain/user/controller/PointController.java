package com.example.coffeeordersystem.domain.user.controller;

import com.example.coffeeordersystem.domain.user.dto.request.PointChargeRequest;
import com.example.coffeeordersystem.domain.user.dto.response.PointChargeResponse;
import com.example.coffeeordersystem.domain.user.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/points/charge")
    public PointChargeResponse chargePoint(@RequestBody PointChargeRequest request) {
        return pointService.chargePoint(request.getUserId(), request.getAmount());
    }
}