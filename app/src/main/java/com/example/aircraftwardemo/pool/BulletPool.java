package com.example.aircraftwardemo.pool;

import com.example.aircraftwardemo.model.EnemyBullet;
import com.example.aircraftwardemo.model.HeroBullet;

public class BulletPool {
    // 使用通用池，分别创建英雄和敌机的对象池
    private static final ObjectPool<HeroBullet> heroPool = new ObjectPool<>();
    private static final ObjectPool<EnemyBullet> enemyPool = new ObjectPool<>();

    // --- 英雄子弹 ---
    public static HeroBullet getHeroBullet(int x, int y, int speedX, int speedY, int power) {
        // 1. 调用acquire()
        HeroBullet bullet = heroPool.acquire();

        if (bullet == null) {
            // 2. 如果 acquire 返回 null，说明池子空了，new 一个
            bullet = new HeroBullet(x, y, speedX, speedY, power);
        } else {
            // 3. 如果拿到了，重置属性
            bullet.reInit(x, y, speedX, speedY, power);
        }
        return bullet;
    }

    public static void recycleH(HeroBullet bullet) {
        // 直接调用release()，它会自动帮你做非空和重复校验
        heroPool.release(bullet);
    }

    // --- 敌机子弹同理 ---
    public static EnemyBullet getEnemyBullet(int x, int y, int speedX, int speedY, int power) {
        EnemyBullet bullet = enemyPool.acquire();
        if (bullet == null) {
            bullet = new EnemyBullet(x, y, speedX, speedY, power);
        } else {
            bullet.reInit(x, y, speedX, speedY, power);
        }
        return bullet;
    }

    public static void recycleE(EnemyBullet bullet) {
        enemyPool.release(bullet);
    }
}
