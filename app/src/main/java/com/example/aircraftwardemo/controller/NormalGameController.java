package com.example.aircraftwardemo.controller;

import android.content.Context;

import com.example.aircraftwardemo.manager.EnemySpawnManager;

public class NormalGameController extends GameController {

    private int dynamicEnemyMaxNumber = 6;
    private int lastDifficultyUpdateTime = 0;

    public NormalGameController(boolean soundEnabled, Context context) {
        super(soundEnabled, context);
        // 简单模式初始化
        enemyMaxNumber = 6;
        BOSS_SCORE_THRESHOLD = 600;

        enemySpawnManager = new EnemySpawnManager(
                mobConfig, eliteConfig, plusConfig, bossConfig,
                75, 15, 10, BOSS_SCORE_THRESHOLD,
                this
        );
    }

    @Override
    protected int getEnemyMaxNumber() {
        return dynamicEnemyMaxNumber;
    }

    @Override
    protected void onTimeIncrease() {
        int currentTime = time;
        if (currentTime - lastDifficultyUpdateTime >= 1000) {
            lastDifficultyUpdateTime = currentTime;

            // 提升敌机生成频率
            float currentSpawnInterval = enemySpawnScheduler.getInterval();
            float newSpawnInterval = Math.max(200f, currentSpawnInterval - 20f);
            enemySpawnScheduler.setInterval(newSpawnInterval);

            // 提升英雄射击频率
            float currentHeroShootInterval = heroShootScheduler.getInterval();
            float newHeroShootInterval = Math.max(150f, currentHeroShootInterval - 10f);
            heroShootScheduler.setInterval(newHeroShootInterval);

            // 动态增加敌机上限
            dynamicEnemyMaxNumber += 1;
        }
    }
}
