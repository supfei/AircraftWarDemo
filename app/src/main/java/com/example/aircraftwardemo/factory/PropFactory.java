package com.example.aircraftwardemo.factory;

import com.example.aircraftwardemo.model.AbstractProp;

public interface PropFactory {
    AbstractProp createProp(int locationX, int locationY, int speedX, int speedY);
}
