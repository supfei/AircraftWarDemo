package com.example.aircraftwardemo.factory;

import com.example.aircraftwardemo.model.AbstractProp;
import com.example.aircraftwardemo.model.PropBlood;

/**
 * @author 有朝宿云
 * Created on 2025/10/02 00:30
 **/
public class PropBloodFactory implements PropFactory {
    @Override
    public AbstractProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new PropBlood(locationX, locationY, speedX, speedY);
    }
}