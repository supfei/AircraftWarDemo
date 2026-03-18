package com.example.aircraftwardemo.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.example.aircraftwardemo.manager.EnemySpawnManager;
import com.example.aircraftwardemo.manager.ImageManager;

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


    @Override
    protected void drawBackground(Canvas canvas) {
        Bitmap background = null;

        if (this.score > 2000) {
            background = ImageManager.BACKGROUND4_IMAGE;
        } else {
            background = ImageManager.BACKGROUND2_IMAGE;
        }
        if (background != null) {
            // 简单平铺背景
            int bgHeight = background.getHeight();
            for (int y = 0; y < screenHeight; y += bgHeight) {
                canvas.drawBitmap(background, 0, y, paint);
            }
        } else {
            // 没有背景图片时使用纯色
            canvas.drawColor(Color.BLACK);
        }
    }
}
