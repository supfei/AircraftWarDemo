package com.example.aircraftwardemo.model;

import com.example.aircraftwardemo.strategy.ShootStrategy;
import com.example.aircraftwardemo.strategy.StraightShootStrategy;

import java.util.List;
//test
/**
 * 所有种类飞机的抽象父类：
 * 敌机（BOSS, ELITE, MOB），英雄飞机
 *
 * @author hitsz
 */
public abstract class AbstractAircraft extends AbstractFlyingObject {
    /**
     * 生命值
     */
    private int maxHp;
    private int hp;

    // 射击策略相关属性
    private int shootNum = 1;
    private int power = 15;
    private int direction = -1;
    private ShootStrategy shootStrategy;

    public void setShootStrategy(ShootStrategy shootStrategy) {
        this.shootStrategy = shootStrategy;
    }

    public int getShootNum() {
        return shootNum;
    }

    public void setShootNum(int shootNum) {
        this.shootNum = shootNum;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public AbstractAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY);
        this.hp = hp;
        this.maxHp = hp;
        // 默认射击策略直射
        this.shootStrategy = new StraightShootStrategy();
    }

    public void decreaseHp(int decrease){
        hp -= decrease;
        if(hp <= 0){
            hp=0;
            vanish();
        }
    }

    public void increaseHp(int increase) {
        this.hp += increase;
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        }
    }

    public int getHp() {
        return hp;
    }


    /**
     * 飞机射击方法，可射击对象必须实现
     * @return
     *  可射击对象需实现，返回子弹
     *  非可射击对象空实现，返回null
     */
    public List<BaseBullet> shoot() {
        // 如果飞机已死亡或无效，不发射子弹
        if (this.getHp() <= 0 || !this.isValid) {
            return null;
        }
        // 否则，调用当前策略发射子弹
        return shootStrategy.shoot(this);
    }

    public abstract BaseBullet createBullet(int x, int y, int speedX, int speedY, int power);
}


