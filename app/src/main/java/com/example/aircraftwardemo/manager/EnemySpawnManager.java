package com.example.aircraftwardemo.manager;

import com.example.aircraftwardemo.controller.GameController;
import com.example.aircraftwardemo.factory.BossEnemyFactory;
import com.example.aircraftwardemo.factory.EliteEnemyFactory;
import com.example.aircraftwardemo.factory.ElitePlusEnemyFactory;
import com.example.aircraftwardemo.factory.MobEnemyFactory;
import com.example.aircraftwardemo.model.EnemyAircraft;
import com.example.aircraftwardemo.model.EnemyConfig;


import java.util.Random;

/**
 * 敌机生成管理器：负责根据配置化的概率、属性、条件，生成不同类型的敌机
 */
public class EnemySpawnManager {

    private final Random random = new Random();

    // ========== 敌机配置（由外部传入） ==========
    private final EnemyConfig mobConfig;
    private final EnemyConfig eliteConfig;
    private final EnemyConfig plusConfig;
    private final EnemyConfig bossConfig;

    // ========== 敌机生成概率（百分比，总和建议 <= 100） ==========
    private final int percentMob;     // 普通小兵
    private final int percentElite;   // 精英
    private final int percentPlus;    // Plus


    // ========== Boss 生成条件 ==========
    private final int bossScoreThreshold; // Boss 出现的最低分数
    private boolean hasBoss = false;     // 当前是否已存在 Boss
    private int nextBossScore;           // 下一个 Boss 触发分数阈值

    /**
     * 构造函数：传入所有敌机类型的配置 + 生成概率 + Boss 分数阈值
     */
    private GameController game;  // 新增

    public EnemySpawnManager(
            EnemyConfig mobConfig,
            EnemyConfig eliteConfig,
            EnemyConfig plusConfig,
            EnemyConfig bossConfig,
            int percentMob,
            int percentElite,
            int percentPlus,
            int bossScoreThreshold,
            GameController game  // 新增参数
    ) {
        this.mobConfig = mobConfig;
        this.eliteConfig = eliteConfig;
        this.plusConfig = plusConfig;
        this.bossConfig = bossConfig;
        this.percentMob = percentMob;
        this.percentElite = percentElite;
        this.percentPlus = percentPlus;
        this.bossScoreThreshold = bossScoreThreshold;
        this.game = game;  // 保存引用
        this.nextBossScore = Math.max(1, bossScoreThreshold);
    }

    /**
     * 根据当前游戏状态生成一个敌机
     * @param screenWidth  屏幕宽度，用于生成 X 坐标
     * @param screenHeight 屏幕高度，用于生成 Y 坐标
     * @param currentScore 当前游戏分数，用于判断 Boss 生成条件
     * @return 一个 EnemyAircraft 实例，可能为 null（不生成）
     */
    public EnemyAircraft generateEnemy(int screenWidth, int screenHeight, int currentScore) {
        int rand = random.nextInt(100);

        // ====== 优先级逻辑：Boss > Plus > Elite > Mob ======

        // 1. Boss 生成逻辑（优先判断，有分数门槛 & 唯一性）
        if (!hasBoss && currentScore >= nextBossScore) {
            hasBoss = true;
            nextBossScore += Math.max(1, bossScoreThreshold);
//            // 如果游戏开启了声音，播放 Boss 背景音乐
//            if (game != null && game.soundEnabled) {
//                game.getMusicManager().playBossBackgroundMusic();
//            }

            return createBoss(screenWidth, screenHeight);
        }

        // 2. 普通小兵（Mob）
        if (rand < percentMob) {
            return createMobEnemy(screenWidth, screenHeight);
        }
        // 3. 精英敌机（Elite）
        else if (rand < percentMob + percentElite) {
            return createEliteEnemy(screenWidth, screenHeight);
        }
        // 4. Plus 敌机
        else if (rand < percentMob + percentElite + percentPlus) {
            return createPlusEnemy(screenWidth, screenHeight);
        }

        // 其它情况：不生成敌机
        return null;
    }

    /**
     * 当 Boss 被击败时调用，重置 hasBoss 状态
     */
    public void onBossDestroyed() {
        hasBoss = false;
    }
    public void noBoss() {
        hasBoss = true;
    }

    // ====== 敌机创建方法：调用工厂，并传入对应的 EnemyConfig ======

    private EnemyAircraft createMobEnemy(int screenWidth, int screenHeight) {
        // System.out.println("mob");
        return new MobEnemyFactory().createEnemy(
                random.nextInt(screenWidth - ImageManager.MOB_ENEMY_IMAGE.getWidth()),
                random.nextInt(screenHeight * 5 / 100), // 从屏幕顶部区域生成
                mobConfig.getSpeedX(), mobConfig.getSpeedY(), mobConfig.getHp(), mobConfig.getPropDropCount()
        );
    }

    private EnemyAircraft createEliteEnemy(int screenWidth, int screenHeight) {
        // System.out.println("elite");
        return new EliteEnemyFactory().createEnemy(
                random.nextInt(screenWidth - ImageManager.ELITE_ENEMY_IMAGE.getWidth()),
                random.nextInt(screenHeight * 5 / 100),
                eliteConfig.getSpeedX(), eliteConfig.getSpeedY(), eliteConfig.getHp(), eliteConfig.getPropDropCount()
        );
    }

    private EnemyAircraft createPlusEnemy(int screenWidth, int screenHeight) {
        // System.out.println("plus");
        return new ElitePlusEnemyFactory().createEnemy(
                random.nextInt(screenWidth - ImageManager.ELITE_ENEMY_IMAGE.getWidth()),
                random.nextInt(screenHeight * 5 / 100),
                plusConfig.getSpeedX(), plusConfig.getSpeedY(), plusConfig.getHp(), plusConfig.getPropDropCount()
        );
    }

    private EnemyAircraft createBoss(int screenWidth, int screenHeight) {
        // System.out.println("boss");
        return new BossEnemyFactory().createEnemy(
                random.nextInt(screenWidth - ImageManager.BOSS_ENEMY_IMAGE.getWidth()),
                random.nextInt(screenHeight * 5 / 100),
                bossConfig.getSpeedX(), bossConfig.getSpeedY(), bossConfig.getHp(), bossConfig.getPropDropCount()
        );
    }
}