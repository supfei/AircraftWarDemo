// TouchController.java
package com.example.aircraftwardemo.controller;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.aircraftwardemo.model.HeroAircraft;

/**
 * 触摸控制器
 * 监听触摸事件，控制英雄机的移动
 * Android版本替代原来的HeroController
 */
public class TouchController implements View.OnTouchListener {

    private HeroAircraft heroAircraft;
    private int screenWidth;
    private int screenHeight;

    /**
     * 构造函数
     * @param heroAircraft 要控制的英雄机实例
     */
    public TouchController(HeroAircraft heroAircraft) {
        this.heroAircraft = heroAircraft;
        // 获取屏幕尺寸
        this.screenWidth = GameConfig.getScreenWidth();
        this.screenHeight = GameConfig.getScreenHeight();
    }

    /**
     * 设置屏幕尺寸（当屏幕方向变化时需要调用）
     */
    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下
                handleTouch(event.getX(), event.getY());
                return true;

            case MotionEvent.ACTION_MOVE:
                // 手指移动
                handleTouch(event.getX(), event.getY());
                return true;

            case MotionEvent.ACTION_UP:
                // 手指抬起
                // 可以在这里添加释放操作的逻辑
                return true;

            case MotionEvent.ACTION_CANCEL:
                // 触摸取消
                return true;
        }

        return false;
    }

    /**
     * 处理触摸位置
     */
    private void handleTouch(float x, float y) {
        if (heroAircraft == null) {
            Log.e("TouchController", "英雄机为空");
            return;
        }

        // 将触摸位置限制在屏幕范围内
        int touchX = (int) Math.max(0, Math.min(x, screenWidth));
        int touchY = (int) Math.max(0, Math.min(y, screenHeight));

        // 更新英雄机位置
        heroAircraft.setLocation(touchX, touchY);

        // 调试日志
        Log.d("TouchController",
                String.format("触摸位置: (%d, %d), 英雄机位置: (%d, %d)",
                        touchX, touchY,
                        heroAircraft.getLocationX(), heroAircraft.getLocationY()));
    }
}