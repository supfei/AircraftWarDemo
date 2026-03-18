// MainActivity.java
package com.example.aircraftwardemo.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
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
        // Activity暂停时，可以暂停游戏
        if (gameView != null) {
            // 可以添加暂停逻辑
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Activity恢复时，可以恢复游戏
        if (gameView != null) {
            // 可以添加恢复逻辑
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源
        if (gameView != null) {
            // 可以添加清理逻辑
        }
    }
}