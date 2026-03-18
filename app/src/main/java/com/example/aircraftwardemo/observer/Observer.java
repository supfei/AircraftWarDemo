package com.example.aircraftwardemo.observer;

import com.example.aircraftwardemo.model.PropBomb;

/**
 * 观察者接口：所有受炸弹影响的对象（敌机、子弹、英雄机等）需实现此接口
 */
public interface Observer {
    /**
     * 当炸弹爆炸时，此方法被调用，对象根据自身类型做出响应
     * @param bomber 爆炸的炸弹对象（可传递额外信息，如位置、范围等）
     */
    void update(PropBomb bomber);
}