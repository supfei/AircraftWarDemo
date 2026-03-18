package com.example.aircraftwardemo.model;

/**
 * @author 有朝宿云
 * Created on 2025/09/21 12:07
 **/
public abstract class AbstractProp extends AbstractFlyingObject {
    public AbstractProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    /**
     * 当道具被英雄机拾取（或碰撞）时触发，实现该道具的具体功能
     * @param hero 飞行器对象
     */
    public abstract void applyEffect(HeroAircraft hero);
}