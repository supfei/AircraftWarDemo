// MainActivity.java
package com.example.aircraftwardemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aircraftwardemo.manager.AudioManager;
import com.example.aircraftwardemo.model.HeroAircraft;
import com.example.aircraftwardemo.network.match.MultiplayerSessionManager;
import com.example.aircraftwardemo.view.GameView;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;
    public static Socket socket;
    private boolean isMultiplayer;
    private String roomId;
    private String playerId;
    private MultiplayerSessionManager sessionManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable scoreSyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取从主菜单传过来的游戏模式
        Intent intent = getIntent();
        String gameMode = intent.getStringExtra("game_mode");
        isMultiplayer = intent.getBooleanExtra("is_multiplayer", false);
        roomId = intent.getStringExtra("room_id");
        playerId = intent.getStringExtra("player_id");
        // 如果没有传过来，默认为简单模式
        if (gameMode == null) {gameMode = "easy";}

        // 不再从 Intent 读取音乐开关，直接使用 AudioManager 的状态
        boolean soundEnable = AudioManager.getInstance().isMusicOn();
        // 每次进入游戏，背景音乐从头播放
        AudioManager.getInstance().restartBGM();

        // 创建GameView（简单模式）
        gameView = new GameView(this, gameMode, soundEnable);
        gameView.setMultiplayerEnabled(isMultiplayer);
        gameView.setEnemyScore(-1);

        // 设置为全屏
        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // 设置ContentView
        setContentView(gameView);

        if (isMultiplayer && roomId != null && playerId != null) {
            sessionManager = new MultiplayerSessionManager();
            startScoreSyncLoop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "Activity暂停");

        if (gameView != null) {
            gameView.pauseGame();
        }
        stopScoreSyncLoop();
        // 页面暂停时关闭音乐
        AudioManager.getInstance().pauseBGM();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "Activity恢复");

        if (gameView != null) {
            gameView.resumeGame();
        }
        if (isMultiplayer && sessionManager != null) {
            startScoreSyncLoop();
        }

        // 根据 AudioManager 中的当前开关状态恢复音乐
        if (AudioManager.getInstance().isMusicOn()) {
            AudioManager.getInstance().playBGM();
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
        stopScoreSyncLoop();

        // 清理HeroAircraft单例（如果需要重新开始游戏）
        HeroAircraft.clearInstance();
    }

    private void startScoreSyncLoop() {
        stopScoreSyncLoop();
        scoreSyncTask = new Runnable() {
            @Override
            public void run() {
                if (sessionManager == null || gameView == null) {
                    return;
                }
                int localScore = gameView.getCurrentScore();
                sessionManager.syncScore(roomId, playerId, localScore, new MultiplayerSessionManager.ScoreSyncCallback() {
                    @Override
                    public void onSuccess(int enemyScore) {
                        if (gameView != null) {
                            gameView.setEnemyScore(enemyScore);
                        }
                        handler.postDelayed(scoreSyncTask, 400);
                    }

                    @Override
                    public void onFailure(String error) {
                        handler.postDelayed(scoreSyncTask, 800);
                    }
                });
            }
        };
        handler.post(scoreSyncTask);
    }

    private void stopScoreSyncLoop() {
        if (scoreSyncTask != null) {
            handler.removeCallbacks(scoreSyncTask);
        }
    }

}