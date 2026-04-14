package com.example.aircraftwardemo.model;

import android.os.Handler;
import android.os.Looper;

import com.example.aircraftwardemo.manager.ShootStrategyManager;

/**
 * @author 有朝宿云
 * Created on 2025/10/17 11:19
 **/
public class PropBulletPlus extends AbstractProp{
    private static final long EFFECT_DURATION_MS = 10_000L;

    public PropBulletPlus(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }
    @Override
    public void applyEffect(HeroAircraft hero) {
        hero.setShootNum(20);
        // System.out.println("PlusFireSupply active!");
        // 1. 获取策略管理器
        ShootStrategyManager strategyManager = hero.getShootStrategyManager();
        if (strategyManager != null) {
            // 2. 切换为散射模式（直接操作管理器，而不是调用 hero 的 set 方法）
            strategyManager.applyCircleEffect();

            // 3. 10秒后恢复直射
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                hero.setShootNum(1);
                strategyManager.applyStraightEffect();
                // System.out.println("散射效果结束，已恢复直射。");
            }, EFFECT_DURATION_MS);
        }

    }
}