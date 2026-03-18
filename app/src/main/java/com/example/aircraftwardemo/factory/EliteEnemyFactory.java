package com.example.aircraftwardemo.factory;

import com.example.aircraftwardemo.model.EliteEnemy;
import com.example.aircraftwardemo.model.EnemyAircraft;

/**
 * @author 有朝宿云
 * Created on 2025/09/25 21:40
 **/
public class EliteEnemyFactory implements EnemyFactory{
    @Override
    public EnemyAircraft createEnemy(int locationX, int locationY, int speedX, int speedY, int hp, int propDropCount) {
        return new EliteEnemy(locationX, locationY, speedX, speedY, hp, propDropCount);
    }
}