package com.example.aircraftwardemo.strategy;

import com.example.aircraftwardemo.model.AbstractAircraft;
import com.example.aircraftwardemo.model.BaseBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * @author 有朝宿云
 * Created on 2025/10/15 21:50
 **/
public class StraightShootStrategy implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + aircraft.getDirection()*2;
        int speedX = 0;
        int speedY = aircraft.getSpeedY() + aircraft.getDirection()*5;
        BaseBullet bullet;
        for(int i=0; i<aircraft.getShootNum(); i++){
            // 子弹发射位置相对飞机位置向前偏移
            // 多个子弹横向分散
            bullet = aircraft.createBullet(x + (i * 2 - aircraft.getShootNum() + 1) * 10, y, speedX, speedY, aircraft.getPower());
            res.add(bullet);
        }
        return res;
    }
}