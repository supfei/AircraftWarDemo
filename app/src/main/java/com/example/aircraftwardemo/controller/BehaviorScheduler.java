package com.example.aircraftwardemo.controller;

/**
 * 通用的行为调度器（定时触发器）
 * 用于控制某一行为（如敌机生成、射击等）以固定的时间间隔周期性执行。
 *
 * 通过传入一个 Runnable 行为逻辑和间隔时间（毫秒），在每次调用 update(deltaTime) 时，
 * 内部会累积时间并在达到间隔时触发一次行为。
 */
public class BehaviorScheduler {

    private float timer = 0f;           // 当前已累积时间（单位：毫秒）
    private float interval = 500f;     // 触发间隔，单位毫秒
    private Runnable action;            // 每次触发时要执行的逻辑
    private boolean enabled = true;     // 是否启用该调度器

    /**
     * 构造函数
     *
     * @param action   要周期性执行的逻辑（任务），通过 Runnable 接口传入
     * @param interval 触发间隔，单位毫秒（例如：1000 表示每 1 秒触发一次）
     */
    public BehaviorScheduler(Runnable action, float interval) {
        this.action = action;
        this.interval = interval;
    }

    /**
     * 更新调度器状态，需每帧调用，传入该帧的时间间隔（单位：毫秒）
     *
     * @param deltaTime 本帧经过的时间，单位毫秒（例如：40ms）
     */
    public void update(float deltaTime) {
        if (!enabled) return;

        timer += deltaTime;
        if (timer >= interval) {
            // 达到触发时间，执行行为
            timer = 0f;      // 重置计时器
            if (action != null) {
                action.run(); // 触发行为逻辑
            }
        }
    }

    // ======== Getter & Setter 方法 ========

    /**
     * 设置触发间隔（单位：毫秒）
     */
    public void setInterval(float interval) {
        this.interval = interval;
    }

    /**
     * 获取当前触发间隔（单位：毫秒）
     */
    public float getInterval() {
        return interval;
    }

    /**
     * 设置是否启用该调度器
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取当前是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置调度器要执行的动作逻辑
     */
    public void setAction(Runnable action) {
        this.action = action;
    }

    /**
     * 手动触发一次行为（不依赖计时器）
     */
    public void trigger() {
        if (action != null) {
            action.run();
        }
    }

    /**
     * 重置计时器（但不触发行为）
     */
    public void resetTimer() {
        timer = 0f;
    }
}