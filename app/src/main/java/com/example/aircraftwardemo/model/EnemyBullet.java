package com.example.aircraftwardemo.model;

import com.example.aircraftwardemo.observer.Observer;

/**
 * @Author hitsz
 */
public class EnemyBullet extends BaseBullet implements Observer {

    public EnemyBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
    }

    @Override
    public void update(PropBomb bomber) {
        // 敌机子弹被炸弹清除
        this.vanish();
        // System.out.println("敌机子弹被炸弹清除: EnemyBullet");
    }
}
