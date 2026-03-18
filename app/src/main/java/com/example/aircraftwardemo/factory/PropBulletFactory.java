package com.example.aircraftwardemo.factory;

import com.example.aircraftwardemo.model.AbstractProp;
import com.example.aircraftwardemo.model.PropBullet;

/**
 * @author 有朝宿云
 * Created on 2025/10/02 00:49
 **/
public class PropBulletFactory implements PropFactory{
    @Override
    public AbstractProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new PropBullet(locationX, locationY, speedX, speedY);
    }
}