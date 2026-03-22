package com.example.aircraftwardemo.model;

//import com.example.aircraftwardemo.controller.AbstractGame;
import com.example.aircraftwardemo.controller.GameController;
import com.example.aircraftwardemo.observer.Observer;
import com.example.aircraftwardemo.observer.Subject;

import java.util.ArrayList;
import java.util.List;



public class PropBomb extends AbstractProp implements Subject {

    protected List<Observer> observers = new ArrayList<>();
//    protected Game game; // 用于直接访问当前游戏中的敌机和子弹
//    // ⭐ 重要：让 Game 能传进来，从而访问敌机列表和子弹列表
//    public void setGame(Game game) {
//        this.game = game;
//    }模板

    protected GameController game; // 用于直接访问当前游戏中的敌机和子弹
    // ⭐ 重要：让 Game 能传进来，从而访问敌机列表和子弹列表
    public void setGame(GameController game) {
        this.game = game;
    }


    public PropBomb(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }


    @Override
    public void applyEffect(HeroAircraft hero) {
        // System.out.println("💣 炸弹道具生效！");

        if (game == null) {
            // System.out.println("警告：PropBomb 没有绑定 Game，无法直接清除敌机");
            notifyObservers(); // 仅通知观察者
            return;
        }

        // ===== 模式一：直接清除当前游戏中的敌机和子弹 =====
        clearCurrentEnemiesAndBullets();

        // ===== 模式二：通知已注册的观察者（未来敌机/子弹） =====
        notifyObservers();
    }

    /**
     * 模式一：直接遍历并清除当前 Game 中的敌机和子弹
     */
    protected void clearCurrentEnemiesAndBullets() {
        if (game == null) return;

        int scoreGained = 0;

        // -------- 1. 敌机处理 --------
        List<EnemyAircraft> toRemoveEnemies = new ArrayList<>();
        for (EnemyAircraft enemy : game.getEnemyAircrafts()) {
            if (enemy.notValid()) continue;

            if (enemy instanceof MobEnemy || enemy instanceof EliteEnemy) {
                // 普通、精英敌机：直接清除
                toRemoveEnemies.add(enemy);
                // System.out.println("炸弹清除了敌机: " + enemy.getClass().getSimpleName());
            }
            else if (enemy instanceof ElitePlusEnemy) {
                // 超级精英：减血
                enemy.decreaseHp(15); // 可以调整伤害值
                // System.out.println("炸弹减少了超级精英敌机血量: " + enemy.getClass().getSimpleName());
            }
            else if (enemy instanceof BossEnemy) {
                // Boss：不受影响
                // System.out.println("Boss 敌机不受炸弹影响");
            }
        }

        // 移除普通/精英敌机，增加分数
        for (EnemyAircraft enemy : toRemoveEnemies) {
            scoreGained += enemy.getScore();
            enemy.vanish(); // 标记为无效，后续被清理
        }

        // -------- 2. 敌机子弹处理 --------
        List<BaseBullet> toRemoveBullets = new ArrayList<>();
        for (BaseBullet bullet : game.getEnemyBullets()) {
            if (bullet.notValid()) continue;

            if (bullet instanceof EnemyBullet) {
                toRemoveBullets.add(bullet);
                // System.out.println("炸弹清除了敌机子弹");
            }
        }

        for (BaseBullet bullet : toRemoveBullets) {
            bullet.vanish();
        }

        // -------- 3. 让英雄机获得分数 --------
        if (scoreGained > 0) {
            game.addScore(scoreGained);
            // System.out.println("🎯 英雄机通过炸弹（遍历）获得分数: " + scoreGained);
        }
    }

    // ========== 观察者模式相关 ==========

    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this); // 炸弹通知观察者（敌机/子弹）自毁
        }
    }
}