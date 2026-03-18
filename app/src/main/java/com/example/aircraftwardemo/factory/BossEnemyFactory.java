package com.example.aircraftwardemo.factory;

import com.example.aircraftwardemo.model.BossEnemy;
import com.example.aircraftwardemo.model.EnemyAircraft;

/**
 * @author 有朝宿云
 * Created on 2025/10/08 16:18
 **/
public class BossEnemyFactory implements EnemyFactory{
    @Override
    public EnemyAircraft createEnemy(int locationX, int locationY, int speedX, int speedY, int hp, int propDropCount) {
        return new BossEnemy(locationX, locationY, speedX, speedY, hp,propDropCount);
    }
}