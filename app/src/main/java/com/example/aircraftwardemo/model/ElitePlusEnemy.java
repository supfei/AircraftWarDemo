package com.example.aircraftwardemo.model;

import com.example.aircraftwardemo.strategy.ScatterShootStrategy;

public class ElitePlusEnemy extends EnemyAircraft{

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp,  int propDropCount) {
        super(locationX, locationY, speedX, speedY, hp,  propDropCount);
        this.setShootNum(3);
        this.setPower(5);
        this.setDirection(1);
        this.setShootStrategy(new ScatterShootStrategy());
    }


    @Override
    public int getScore() {
        return 30;
    }

    @Override
    public void update(PropBomb bomber) {
        // 超级精英敌机被炸弹击中时，只扣血，不消失
        this.decreaseHp(15);
        // System.out.println("超级精英敌机被炸弹击中，HP-15，未被清除！");
    }
}