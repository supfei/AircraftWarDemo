package com.example.aircraftwardemo.factory;

import com.example.aircraftwardemo.model.EnemyAircraft;
import com.example.aircraftwardemo.model.MobEnemy;

/**
 * @author 有朝宿云
 * Created on 2025/09/25 21:34
 **/
public class MobEnemyFactory implements EnemyFactory{
    @Override
    public EnemyAircraft createEnemy(int locationX, int locationY, int speedX, int speedY, int hp, int propDropCount) {
        return new MobEnemy(locationX, locationY, speedX, speedY, hp, propDropCount);
    }
}