package com.example.coffeeordersystem.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PointChargeRequest {

    private Long userId;
    private Long amount;
}