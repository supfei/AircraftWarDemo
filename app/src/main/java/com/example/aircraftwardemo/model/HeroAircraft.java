package com.example.aircraftwardemo.model;

import android.util.Log;

import com.example.aircraftwardemo.controller.GameConfig;
import com.example.aircraftwardemo.controller.GameController;
import com.example.aircraftwardemo.manager.ImageManager;
import com.example.aircraftwardemo.manager.ShootStrategyManager;

import java.util.List;

/**
 * 英雄飞机，游戏玩家操控
 * @author hitsz
 */
public class HeroAircraft extends AbstractAircraft {

    /**
     * @param locationX 英雄机位置x坐标
     * @param locationY 英雄机位置y坐标
     * @param speedX 英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param speedY 英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param hp    初始生命值
     */

    // 射击策略管理器
    private final ShootStrategyManager shootStrategyManager;
//    private Game game;模板
//
//    // 设置 Game 引用（由 Game 类在创建或绑定 HeroAircraft 时设置）
//    public void setGame(Game game) {
//        this.game = game;
//    }

    private GameController game;

    // 设置 Game 引用（由 Game 类在创建或绑定 HeroAircraft 时设置）
    public void setGame(GameController game) {
        this.game = game;
    }

    // 获取 Game 引用（供 Prop 调用）
    public GameController getGame() {
        return game;
    }

    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootStrategyManager = new ShootStrategyManager(this);
    }

//    //单例模式，懒汉式
//    private static final HeroAircraft instance = new HeroAircraft(
//            GameConfig.getScreenWidth() / 2,
//            GameConfig.getScreenHeight() - ImageManager.HERO_IMAGE.getHeight() ,
//            0, 0, 1000);
//
//    public static HeroAircraft getInstance() {
//        return instance;
//    }
    // 单例模式，硬汉式
private static HeroAircraft instance = null;  // 改为懒汉式

    public static HeroAircraft getInstance() {
        if (instance == null) {
            instance = new HeroAircraft(
                    GameConfig.getScreenWidth() / 2,
                    GameConfig.getScreenHeight() - ImageManager.HERO_IMAGE.getHeight(),
                    0, 0, 1000
            );
        }
        return instance;
    }

    // 添加清理方法
    public static void clearInstance() {
        synchronized (HeroAircraft.class) {
            instance = null;
            Log.d("HeroAircraft", "英雄机单例已清理");
        }
    }

    // HeroAircraft.java
    public void reset() {
        synchronized (this) {
            setHp(1000);  // 重置生命值
            setShootNum(1);
            setPower(30);
// 重置位置
            int screenWidth = GameConfig.getScreenWidth();
            int screenHeight = GameConfig.getScreenHeight();
            int x = screenWidth / 2;
            int y = screenHeight - 200;
            if (ImageManager.HERO_IMAGE != null) {
                y = screenHeight - ImageManager.HERO_IMAGE.getHeight() - 50;
            } else {
                Log.e("HeroAircraft", "reset:图片未加载 ");
            }

            this.setLocation(x, y);
            // 重置射击策略
            if (shootStrategyManager != null) {
                shootStrategyManager.applyStraightEffect();
            }

            Log.d("HeroAircraft", "重置完成: HP=" + getHp() + ", 位置=(" + x + "," + y + ")");
        }
    }
    public ShootStrategyManager getShootStrategyManager() {
        return shootStrategyManager;
    }

    @Override
    public void forward() {
        // 英雄机由鼠标控制，不通过forward函数移动
    }

    @Override
    /**
     * 通过射击产生子弹
     * @return 射击出的子弹List
     */
    public List<BaseBullet> shoot() {
        return shootStrategyManager.executeShoot();
    }

    @Override
    public BaseBullet createBullet(int x, int y, int speedX, int speedY, int power){
        return new HeroBullet(x, y, speedX, speedY, power);
    }
}
