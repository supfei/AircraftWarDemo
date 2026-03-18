package com.example.aircraftwardemo.model;

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

    //单例模式,饿汉式9.25
    private static final HeroAircraft instance = new HeroAircraft(
            GameConfig.getScreenWidth() / 2,
            GameConfig.getScreenHeight() - ImageManager.HERO_IMAGE.getHeight() ,
            0, 0, 1000);

    public static HeroAircraft getInstance() {
        return instance;
    }

    /**攻击方式 */

    /**
     * 子弹一次发射数量shootNum = 1;
     */
//    /**
//     * 切换为直射模式（默认）
//     */
//    public void setShootModeToStraight() {
//        shootStrategyManager.applyStraightEffect();
//    }
//
//    /**
//     * 切换为散射模式（道具效果）
//     */
//    public void setShootModeToScatter() {
//        shootStrategyManager.applyScatterEffect();
//    }
//
//    /**
//     * 切换为环射模式（道具效果）
//     */
//    public void setShootModeToCircle() {
//        shootStrategyManager.applyCircleEffect();
//    }

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
