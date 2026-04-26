package com.example.aircraftwardemo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aircraftwardemo.R;
import com.example.aircraftwardemo.network.match.MultiplayerSessionManager;

public class MatchResultActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_ID = "room_id";
    public static final String EXTRA_PLAYER_ID = "player_id";
    public static final String EXTRA_GAME_MODE = "game_mode";
    public static final String EXTRA_LOCAL_SCORE = "local_score";

    private TextView tvResultTitle;
    private TextView tvResultDesc;
    private TextView tvMyScore;
    private TextView tvEnemyScore;
    private Button btnExit;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pollTask;

    private MultiplayerSessionManager sessionManager;
    private String roomId;
    private String playerId;
    private String gameMode;
    private int localScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_result);

        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        playerId = getIntent().getStringExtra(EXTRA_PLAYER_ID);
        gameMode = getIntent().getStringExtra(EXTRA_GAME_MODE);
        localScore = getIntent().getIntExtra(EXTRA_LOCAL_SCORE, 0);
        if (gameMode == null) {
            gameMode = "normal";
        }

        sessionManager = new MultiplayerSessionManager();

        tvResultTitle = findViewById(R.id.tv_result_title);
        tvResultDesc = findViewById(R.id.tv_result_desc);
        tvMyScore = findViewById(R.id.tv_my_score);
        tvEnemyScore = findViewById(R.id.tv_enemy_score);
        btnExit = findViewById(R.id.btn_exit);

        tvResultTitle.setText("对战结果");
        tvResultDesc.setText("等待对方结束...");
        tvMyScore.setText("" + localScore);
        tvEnemyScore.setText("--");
        btnExit.setEnabled(false);

        btnExit.setOnClickListener(v -> openGameOverPage());
        startPollingResult();
    }

    private void startPollingResult() {
        stopPollingResult();
        pollTask = new Runnable() {
            @Override
            public void run() {
                if (roomId == null || playerId == null) {
                    return;
                }
                sessionManager.getMatchResult(roomId, playerId, new MultiplayerSessionManager.MatchResultCallback() {
                    @Override
                    public void onSuccess(MultiplayerSessionManager.MatchResultResponse response) {
                        tvMyScore.setText("" + response.myScore);
                        tvEnemyScore.setText("" + response.enemyScore);
                        if (response.bothFinished) {
                            if (response.youWin) {
                                tvResultDesc.setText("YOU WIN");
                                tvResultDesc.setTextColor(Color.YELLOW);
                                tvResultDesc.setShadowLayer(30, 0, 0, Color.YELLOW);
                            } else if ("draw".equals(response.winner)) {
                                tvResultDesc.setText("DRAW");
                                tvResultDesc.setTextColor(Color.WHITE);
                                tvResultDesc.setShadowLayer(30, 0, 0, Color.WHITE);
                            } else {
                                tvResultDesc.setText("YOU LOSE");
                                tvResultDesc.setTextColor(Color.RED);
                                tvResultDesc.setShadowLayer(30, 0, 0, Color.RED);
                            }
                            btnExit.setEnabled(true);
                            return;
                        }
                        tvResultDesc.setText("等待对方结束...");
                        handler.postDelayed(pollTask, 1000);
                    }

                    @Override
                    public void onFailure(String error) {
                        handler.postDelayed(pollTask, 1500);
                    }
                });
            }
        };
        handler.post(pollTask);
    }

    private void stopPollingResult() {
        if (pollTask != null) {
            handler.removeCallbacks(pollTask);
        }
    }

    private void openGameOverPage() {
        Intent intent = new Intent(MatchResultActivity.this, GameOverActivity.class);
        intent.putExtra(GameOverActivity.EXTRA_FINAL_SCORE, localScore);
        intent.putExtra(GameOverActivity.EXTRA_GAME_MODE, gameMode);
        intent.putExtra(GameOverActivity.EXTRA_IS_MULTIPLAYER, true);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPollingResult();
    }
}
