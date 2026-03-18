package com.example.aircraftwardemo.factory;

import com.example.aircraftwardemo.model.EnemyAircraft;

public interface EnemyFactory {
    public EnemyAircraft createEnemy(int locationX, int locationY, int speedX, int speedY, int hp, int propDropCount);
}
