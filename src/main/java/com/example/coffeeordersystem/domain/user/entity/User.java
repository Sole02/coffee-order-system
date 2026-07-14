package com.example.coffeeordersystem.domain.user.entity;

import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long point;

    @Version
    private Long version;

    public User(String name, Long point) {
        this.name = name;
        this.point = point == null ? 0L : point;
    }

    public void chargePoint(Long amount) {
        this.point += amount;
    }

    public void usePoint(Long amount) {
        if (this.point < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }
        this.point -= amount;
    }
}