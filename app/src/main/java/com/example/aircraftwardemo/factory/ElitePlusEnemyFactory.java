package com.example.aircraftwardemo.factory;

import com.example.aircraftwardemo.model.ElitePlusEnemy;
import com.example.aircraftwardemo.model.EnemyAircraft;

/**
 * @author 有朝宿云
 * Created on 2025/10/08 16:14
 **/
public class ElitePlusEnemyFactory implements EnemyFactory{
    @Override
    public EnemyAircraft createEnemy(int locationX, int locationY, int speedX, int speedY, int hp, int propDropCount) {
        return new ElitePlusEnemy(locationX, locationY, speedX, speedY, hp, propDropCount);
    }
}