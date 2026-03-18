package com.example.aircraftwardemo.factory;

import com.example.aircraftwardemo.model.AbstractProp;
import com.example.aircraftwardemo.model.PropBulletPlus;

/**
 * @author 有朝宿云
 * Created on 2025/10/17 16:58
 **/
public class PropBulletPlusFactory implements PropFactory{
    @Override
    public AbstractProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new PropBulletPlus(locationX, locationY, speedX, speedY);
    }
}