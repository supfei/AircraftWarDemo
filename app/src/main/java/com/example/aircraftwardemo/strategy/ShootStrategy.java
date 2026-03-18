package com.example.aircraftwardemo.strategy;

import com.example.aircraftwardemo.model.AbstractAircraft;
import com.example.aircraftwardemo.model.BaseBullet;

import java.util.List;

public interface ShootStrategy {
    /**
     * 射击
     * @param aircraft
     * @return 射击出的子弹列表
     */
    List<BaseBullet> shoot(AbstractAircraft aircraft);
}
