package com.example.aircraftwardemo.controller;

import android.content.Context;

import com.example.aircraftwardemo.manager.EnemySpawnManager;

public class HardGameController extends GameController {

    private int dynamicEnemyMaxNumber = 12;
    private int lastDifficultyUpdateTime = 0;
    private int lastEnemyAttrUpdateTime = 0;

    public HardGameController(boolean soundEnabled, Context context) {
        super(soundEnabled, context);
        // 简单模式初始化
        enemyMaxNumber = 12;
        BOSS_SCORE_THRESHOLD = 200;

        enemySpawnManager = new EnemySpawnManager(
                mobConfig, eliteConfig, plusConfig, bossConfig,
                60, 25, 15, BOSS_SCORE_THRESHOLD,
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

        // 每10秒提升频率
        if (currentTime - lastDifficultyUpdateTime >= 10000) {
            lastDifficultyUpdateTime = currentTime;

            // 提升各种频率
            enemySpawnScheduler.setInterval(
                    Math.max(150f, enemySpawnScheduler.getInterval() - 20f)
            );
            heroShootScheduler.setInterval(
                    Math.max(100f, heroShootScheduler.getInterval() - 10f)
            );
            enemyShootScheduler.setInterval(
                    Math.max(200f, enemyShootScheduler.getInterval() - 5f)
            );

            // 动态增加敌机上限
            dynamicEnemyMaxNumber += 2;
        }

        // 每20秒提升敌机属性
        if (currentTime - lastEnemyAttrUpdateTime >= 20000) {
            lastEnemyAttrUpdateTime = currentTime;

            // 提升敌机属性
            mobConfig.setSpeedY(mobConfig.getSpeedY() + 1);
            mobConfig.setHp(mobConfig.getHp() + 5);
            eliteConfig.setHp(eliteConfig.getHp() + 5);
            plusConfig.setHp(plusConfig.getHp() + 5);
            bossConfig.setHp(bossConfig.getHp() + 50);
        }
    }
}