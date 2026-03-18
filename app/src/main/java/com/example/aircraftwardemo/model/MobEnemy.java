package com.example.aircraftwardemo.model;

/**
 * 普通敌机
 * 敌机（BOSS, ELITE, MOB）
 *
 * @author hitsz & 有朝宿云25.0917
 */public class MobEnemy extends EnemyAircraft {

    public MobEnemy(int locationX, int locationY, int speedX, int speedY, int hp, int propDropCount) {
        super(locationX, locationY, speedX, speedY, hp, propDropCount);
        this.setShootNum(0);       //  0 表示不射击
        this.setPower(0);
        this.setDirection(1);
    }

    @Override
    public int getScore() {
        return 10;
    }
}
