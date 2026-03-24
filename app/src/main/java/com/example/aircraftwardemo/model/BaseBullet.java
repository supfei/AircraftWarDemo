package com.example.aircraftwardemo.model;

import com.example.aircraftwardemo.controller.GameConfig;
import com.example.aircraftwardemo.model.AbstractFlyingObject;

/**
 * 子弹类。
 * 也可以考虑不同类型的子弹
 *
 * @author hitsz
 */
public abstract class BaseBullet extends AbstractFlyingObject {

    private int power = 10;

    public BaseBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY);
        this.power = power;
    }

    @Override
    public void forward() {
        super.forward();

        // 判定 x 轴出界
        if (locationX <= 0 || locationX >= GameConfig.getScreenWidth()) {
            vanish();
        }

        // 判定 y 轴出界
        if (speedY > 0 && locationY >= GameConfig.getScreenHeight() ) {
            // 向下飞行出界
            vanish();
        }else if (locationY <= 0){
            // 向上飞行出界
            vanish();
        }
    }

    public int getPower() {
        return power;
    }

    public void reInit( double locationX, double locationY, int speedX, int speedY, int power) {
        super.reInit(locationX, locationY, speedX, speedY);
        this.power = power;
    }
}
