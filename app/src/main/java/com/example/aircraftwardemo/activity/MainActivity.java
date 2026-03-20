// MainActivity.java
package com.example.aircraftwardemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aircraftwardemo.model.HeroAircraft;
import com.example.aircraftwardemo.view.GameView;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;
    public static Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取从主菜单传过来的游戏模式
        Intent intent = getIntent();
        String gameMode = intent.getStringExtra("game_mode");
        // 如果没有传过来，默认为简单模式
        if (gameMode == null) {gameMode = "easy";}

        // 创建GameView（简单模式）
        gameView = new GameView(this, gameMode);

        // 设置为全屏
        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // 设置ContentView
        setContentView(gameView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "Activity暂停");

        if (gameView != null) {
            gameView.pauseGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "Activity恢复");

        if (gameView != null) {
            gameView.resumeGame();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "Activity销毁");

        if (gameView != null) {
            gameView.cleanup();
            gameView = null;
        }

        // 清理HeroAircraft单例（如果需要重新开始游戏）
        HeroAircraft.clearInstance();
    }


}