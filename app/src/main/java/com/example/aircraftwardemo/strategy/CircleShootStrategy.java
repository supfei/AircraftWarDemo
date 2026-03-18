package com.example.aircraftwardemo.strategy;

import com.example.aircraftwardemo.model.AbstractAircraft;
import com.example.aircraftwardemo.model.BaseBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * @author 有朝宿云
 * Created on 2025/10/17 09:39
 **/
public class CircleShootStrategy implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY(); // 子弹发射点（敌机中心）
        int baseSpeed = aircraft.getDirection()*5; // 子弹基础速度
        int bulletCount = aircraft.getShootNum(); // 子弹数量

        for (int i = 0; i < bulletCount; i++) {
            // 计算子弹方向（绕圆周分布）
            double angle = 2 * Math.PI * i / bulletCount; // 0°~360°均匀分布
            int speedX = (int) (baseSpeed * Math.cos(angle));
            int speedY = (int) (baseSpeed * Math.sin(angle));

            BaseBullet bullet = aircraft.createBullet(x, y, speedX, speedY, aircraft.getPower());
            res.add(bullet);
        }
        return res;
    }
}