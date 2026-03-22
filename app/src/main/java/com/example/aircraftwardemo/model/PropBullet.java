package com.example.aircraftwardemo.model;

import com.example.aircraftwardemo.manager.ShootStrategyManager;

/**
 * @author 有朝宿云
 * Created on 2025/09/21 14:30
 **/
public class PropBullet extends AbstractProp {
    public PropBullet(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }
    @Override
    public void applyEffect(HeroAircraft hero) {
        if (hero == null) return;

        hero.setShootNum(3);
        // System.out.println("FireSupply active!");

        ShootStrategyManager strategyManager = hero.getShootStrategyManager();
        if (strategyManager != null) {
            strategyManager.applyScatterEffect(); // 切换为散射模式

            // 🔧 关键修改：不再自己开线程 sleep，而是委托给 Game 调度恢复
            if (hero.getGame() != null) {
                hero.getGame().scheduleBuffRestore(hero, strategyManager);
            } else {
                // System.out.println("⚠️ PropBullet: 无法获取 Game 实例，无法调度自动恢复");
            }

            // System.out.println("✅ 已切换为散射模式，Buff 将在 10 秒后自动恢复");
        } else {
            // System.out.println("⚠️ PropBullet: 未找到 ShootStrategyManager");
        }
    }
}