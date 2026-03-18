package com.example.aircraftwardemo.factory;

import com.example.aircraftwardemo.model.AbstractProp;
import com.example.aircraftwardemo.model.PropBomb;

/**
 * @author 有朝宿云
 * Created on 2025/10/02 00:34
 **/
public class PropBombFactory implements PropFactory{
    @Override
    public AbstractProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new PropBomb(locationX, locationY, speedX, speedY);
    }
}