package com.example.aircraftwardemo.model;

import com.example.aircraftwardemo.strategy.StraightShootStrategy;

/**
 * @author 有朝宿云
 * Created on 2025/09/17 20:25
 **/
public class EliteEnemy extends EnemyAircraft{

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp, int propDropCount) {
        super(locationX, locationY, speedX, speedY, hp, propDropCount);
        this.setShootNum(1);
        this.setPower(5);
        this.setDirection(1);
        this.setShootStrategy(new StraightShootStrategy());
    }

    @Override
    public int getScore() {
        return 20;
    }
}