package com.example.aircraftwardemo.model;

import com.example.aircraftwardemo.strategy.CircleShootStrategy;

/**
 * @author 有朝宿云
 * Created on 2025/10/08
 */
public class BossEnemy extends EnemyAircraft{

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp, int propDropCount) {
        super(locationX, locationY, speedX, speedY, hp, propDropCount);
        // 为 Boss 设置专属射击参数
        this.setShootNum(20);      // 一次发射 20 发
        this.setPower(5);         // 每颗子弹伤害 15
        this.setDirection(1);      // 子弹向下发射
        this.setShootStrategy(new CircleShootStrategy()); // 使用环形策略
    }

    @Override
    public int getScore() {
        return 100;
    }

    @Override
    public void update(PropBomb bomber) {
        // Boss 敌机免疫炸弹
        System.out.println("Boss 敌机免疫炸弹效果，不受影响！");
    }
}