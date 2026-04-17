package com.example.aircraftwardemo.strategy;

import com.example.aircraftwardemo.model.AbstractAircraft;
import com.example.aircraftwardemo.model.BaseBullet;
import com.example.aircraftwardemo.model.EnemyBullet;
import com.example.aircraftwardemo.model.HeroBullet;

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
        int y = aircraft.getLocationY() + aircraft.getDirection() * 2; // 子弹发射点（敌机中心）
        int baseSpeed = aircraft.getDirection()*6; // 子弹基础速度
        int bulletCount = aircraft.getShootNum(); // 子弹数量

        double deltaAngle = 360.0 / bulletCount;
        // 创建环形的子弹
        BaseBullet bullet;
        for (int i = 0; i < bulletCount; i++) {
            // 计算子弹方向（绕圆周分布）
            // 计算当前子弹的发射角度 (单位：度)
            double angle = i * deltaAngle;
            // 将角度转换为弧度，Java的Math.cos/sin使用弧度
            double radians = Math.toRadians(angle);

            // 计算水平速度 speedX
            double speedX = baseSpeed * Math.cos(radians);
            // 计算垂直速度 speedY
            double speedY = baseSpeed * Math.sin(radians);

            if (aircraft.getDirection() == -1) {
                bullet = new HeroBullet(x, y, (int) Math.round(speedX), (int) Math.round(speedY), aircraft.getPower());
            } else {
                bullet = new EnemyBullet(x, y, (int) Math.round(speedX), (int) Math.round(speedY), aircraft.getPower());
            }

            res.add(bullet);
        }
        return res;
    }
}