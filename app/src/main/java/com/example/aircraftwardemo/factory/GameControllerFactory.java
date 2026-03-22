package com.example.aircraftwardemo.factory;

import android.content.Context;

import com.example.aircraftwardemo.controller.*;

/**
 * 游戏控制器工厂：根据难度字符串生产对应的控制器
 */
public class GameControllerFactory {
    public static GameController createController(String gameMode, boolean soundEnabled, Context context) {
        switch (gameMode.toLowerCase()) {
            case "easy":
                return new EasyGameController(soundEnabled, context);
            case "normal":
                return new NormalGameController(soundEnabled, context);
            case "hard":
                return new HardGameController(soundEnabled, context);
            default:
                return new EasyGameController(soundEnabled, context);
        }
    }
}
