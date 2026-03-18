package com.example.aircraftwardemo.manager;

import com.example.aircraftwardemo.model.AbstractAircraft;
import com.example.aircraftwardemo.model.BaseBullet;
import com.example.aircraftwardemo.strategy.CircleShootStrategy;
import com.example.aircraftwardemo.strategy.ScatterShootStrategy;
import com.example.aircraftwardemo.strategy.ShootStrategy;
import com.example.aircraftwardemo.strategy.StraightShootStrategy;

import java.util.List;

/**
 * @author 有朝宿云
 * Created on 2025/10/17 10:46
 **/
public class ShootStrategyManager {
    private ShootStrategy currentStrategy;
    private AbstractAircraft aircraft;

    public ShootStrategyManager(AbstractAircraft aircraft) {
        this.aircraft = aircraft;
        this.currentStrategy = new StraightShootStrategy(); // 默认策略
    }

    public void setStrategy(ShootStrategy strategy) {
        this.currentStrategy = strategy;
    }

    public List<BaseBullet> executeShoot() {
        return currentStrategy.shoot(aircraft);
    }

    // 可以添加一些组合策略的方法
    public void applyScatterEffect() {
        setStrategy(new ScatterShootStrategy());
    }

    public void applyCircleEffect() {
        setStrategy(new CircleShootStrategy());
    }

    public void applyStraightEffect() {
        setStrategy(new StraightShootStrategy());
    }
}