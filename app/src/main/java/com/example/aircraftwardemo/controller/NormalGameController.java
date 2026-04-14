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

    // --- 新增：难度封顶限制 ---
    private final int MAX_ENEMY_COUNT = 12;        // 屏幕上敌机数量上限，建议不超过20，防止碰撞检测计算过载

    public NormalGameController(boolean soundEnabled, Context context) {
        super(soundEnabled, context);
        // 简单模式初始化
        enemyMaxNumber = 6;
        BOSS_SCORE_THRESHOLD = 800;

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
        if (currentTime - lastDifficultyUpdateTime >= 10000) {
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
            if (dynamicEnemyMaxNumber < MAX_ENEMY_COUNT) {
                dynamicEnemyMaxNumber += 1;
            }
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
            int bgHeight = background.getHeight();
            // 向下滚动：从 backGroundTop - bgHeight 开始，确保屏幕顶部被覆盖
            for (int y = backGroundTop - bgHeight; y < screenHeight; y += bgHeight) {
                canvas.drawBitmap(background, 0, y, paint);
            }
            // 更新滚动偏移量
            backGroundTop += scrollSpeed;
            if (backGroundTop >= bgHeight) {
                backGroundTop -= bgHeight;
            }
        } else {
            // 没有背景图片时使用纯色
            canvas.drawColor(Color.BLACK);
        }
    }
    @Override
    protected void resetTime() {
        super.resetTime();
        this.lastDifficultyUpdateTime = 0;
    }
}
