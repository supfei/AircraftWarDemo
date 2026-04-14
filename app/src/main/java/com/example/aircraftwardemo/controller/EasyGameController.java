package com.example.aircraftwardemo.controller;

import android.content.Context;

public class EasyGameController extends GameController {

    public EasyGameController(boolean soundEnabled, Context context) {
        super(soundEnabled, context);
        // 简单模式初始化
        enemyMaxNumber = 5;
        BOSS_SCORE_THRESHOLD = 200;
        enemySpawnManager.noBoss();
    }

    @Override
    protected int getEnemyMaxNumber() {
        return 5;
    }

    @Override
    protected void onTimeIncrease() {
        // 简单模式，难度不递增
    }
}
