package com.example.aircraftwardemo.model;

import com.example.aircraftwardemo.manager.ShootStrategyManager;

/**
 * @author 有朝宿云
 * Created on 2025/10/17 11:19
 **/
public class PropBulletPlus extends AbstractProp{
    public PropBulletPlus(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }
    @Override
    public void applyEffect(HeroAircraft hero) {
        hero.setShootNum(20);
        System.out.println("PlusFireSupply active!");
        // 1. 获取策略管理器
        ShootStrategyManager strategyManager = hero.getShootStrategyManager();
        if (strategyManager != null) {
            // 2. 切换为散射模式（直接操作管理器，而不是调用 hero 的 set 方法）
            strategyManager.applyCircleEffect();

            // 3. 10秒后恢复直射
            new Thread(() -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                hero.setShootNum(1);
                strategyManager.applyStraightEffect();
                System.out.println("散射效果结束，已恢复直射。");
            }).start();
        }

    }
}