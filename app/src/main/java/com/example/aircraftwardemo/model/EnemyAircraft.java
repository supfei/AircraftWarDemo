package com.example.aircraftwardemo.model;

import com.example.aircraftwardemo.controller.GameConfig;
import com.example.aircraftwardemo.observer.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 所有种类敌机的抽象父类：
 * 敌机（BOSS, ELITE, MOB, ELITEPLUS）
 *
 * @author hitsz & 有朝宿云25.0917
 */public abstract class EnemyAircraft extends AbstractAircraft implements Observer {

    // ✅ 新增字段：该敌机被击毁时默认掉落多少个道具（可为 0）
    protected final int propDropCount; // 0 表示不掉落，>0 表示固定掉落数量
    private static final Random random = new Random();

    public EnemyAircraft(int locationX, int locationY, int speedX, int speedY, int hp, int propDropCount) {
        super(locationX, locationY, speedX, speedY, hp);
        this.propDropCount = propDropCount;
    }

    @Override
    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= GameConfig.getScreenHeight() ) {
            vanish();
        }
    }

    /**
     * 该敌机被击毁时，可能掉落 1 ~ propDropCount 个道具
     * 每个道具类型为 0~3 的随机数
     * 有 80% 的概率触发掉落
     */
    public List<Integer> dropPropTypes() {
        List<Integer> propTypes = new ArrayList<>();
        if (propDropCount > 0 && random.nextDouble() < 0.8) { // 80% 概率
            int numDrops = random.nextInt(propDropCount) + 1; // 随机掉 1~propDropCount 个
            for (int i = 0; i < numDrops; i++) {
                int propType = random.nextInt(4); // 道具类型：0~3
                propTypes.add(propType);
            }
        }
        return propTypes;
    }

    public abstract int getScore();

    @Override
    public BaseBullet createBullet(int x, int y, int speedX, int speedY, int power){
        return new EnemyBullet(x, y, speedX, speedY, power);
    }

    private boolean destroyedByBomb = false;

    public boolean isDestroyedByBomb() {
        return destroyedByBomb;
    }

    public void setDestroyedByBomb(boolean destroyedByBomb) {
        this.destroyedByBomb = destroyedByBomb;
    }

    @Override
    public void update(PropBomb bomber) {
        // 默认行为：普通敌机/精英敌机被炸弹清除
        this.setDestroyedByBomb(true);
        this.vanish();
        System.out.println("敌机被炸弹清除（观察者）: " + this.getClass().getSimpleName());
    }
}
