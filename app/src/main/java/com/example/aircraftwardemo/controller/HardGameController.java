package com.example.aircraftwardemo.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.example.aircraftwardemo.manager.EnemySpawnManager;
import com.example.aircraftwardemo.manager.ImageManager;

public class HardGameController extends GameController {

    private int dynamicEnemyMaxNumber = 12;
    private int lastDifficultyUpdateTime = 0;
    private int lastEnemyAttrUpdateTime = 0;

    // --- 新增：难度封顶限制 ---
    private final int MAX_ENEMY_COUNT = 15;        // 屏幕上敌机数量上限，建议不超过20，防止碰撞检测计算过载
    private final int MAX_ENEMY_SPEED_Y = 18;      // 敌机最大下落速度，防止穿透碰撞检测
    private final int MAX_ENEMY_HP = 150;          // 普通敌机最大血量，防止后期打不死
    private final float MIN_SPAWN_INTERVAL = 300f; // 最小生成间隔（毫秒）

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
            if (dynamicEnemyMaxNumber < MAX_ENEMY_COUNT) {
                dynamicEnemyMaxNumber += 2;
            }
        }

        // 每20秒提升敌机属性
        if (currentTime - lastEnemyAttrUpdateTime >= 20000) {
            lastEnemyAttrUpdateTime = currentTime;

            // 提升敌机属性
//            mobConfig.setSpeedY(mobConfig.getSpeedY() + 1);
//            mobConfig.setHp(mobConfig.getHp() + 5);
//            eliteConfig.setHp(eliteConfig.getHp() + 5);
//            plusConfig.setHp(plusConfig.getHp() + 5);
//            bossConfig.setHp(bossConfig.getHp() + 50);
            // 限制速度
            int newSpeed = Math.min(MAX_ENEMY_SPEED_Y, mobConfig.getSpeedY() + 1);
            mobConfig.setSpeedY(newSpeed);

            // 限制血量
            mobConfig.setHp(Math.min(MAX_ENEMY_HP, mobConfig.getHp() + 5));
            eliteConfig.setHp(Math.min(MAX_ENEMY_HP + 50, eliteConfig.getHp() + 5));
            plusConfig.setHp(Math.min(MAX_ENEMY_HP + 100, plusConfig.getHp() + 5));

            // BOSS血量也可以酌情封顶
            bossConfig.setHp(Math.min(2000, bossConfig.getHp() + 50));
        }
    }

    @Override
    protected void drawBackground(Canvas canvas) {
        Bitmap background = null;

        if (this.score > 2000) {
            background = ImageManager.BACKGROUND5_IMAGE;
        } else {
            background = ImageManager.BACKGROUND3_IMAGE;
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
        this.time = 0;
        this.lastDifficultyUpdateTime = 0;
        this.lastEnemyAttrUpdateTime = 0;
    }
}