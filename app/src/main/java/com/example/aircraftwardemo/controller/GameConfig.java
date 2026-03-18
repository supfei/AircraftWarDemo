package com.example.aircraftwardemo.controller;

/**
 * 游戏配置类，存储屏幕尺寸等全局配置
 */
public class GameConfig {
    private static int screenWidth = 480;  // 默认值
    private static int screenHeight = 800; // 默认值

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    public static void setScreenSize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }
}