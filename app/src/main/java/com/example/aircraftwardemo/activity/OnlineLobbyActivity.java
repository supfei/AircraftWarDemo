package com.example.aircraftwardemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aircraftwardemo.R;
import com.example.aircraftwardemo.network.match.MultiplayerSessionManager;

public class OnlineLobbyActivity extends AppCompatActivity {

    private TextView tvRoomCode;
    private TextView tvMatchStatus;
    private EditText etRoomCode;
    private ProgressBar progressMatch;
    private Button btnStartGame;

    private MultiplayerSessionManager sessionManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable roomStatusPollTask;

    private String roomId;
    private String playerId;
    private String gameMode;
    private boolean isHost;
    private boolean isMatched;
    private boolean gameStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_lobby);

        sessionManager = new MultiplayerSessionManager();
        playerId = sessionManager.createPlayerId();
        gameMode = getIntent().getStringExtra("game_mode");
        if (gameMode == null) {
            gameMode = "normal";
        }

        initViews();
        setupListeners();
        createRoom();
    }

    private void initViews() {
        tvRoomCode = findViewById(R.id.tv_room_code);
        tvMatchStatus = findViewById(R.id.tv_match_status);
        etRoomCode = findViewById(R.id.et_room_code);
        progressMatch = findViewById(R.id.progress_match);
        btnStartGame = findViewById(R.id.btn_start_game);
    }

    private void setupListeners() {
        Button btnJoinRoom = findViewById(R.id.btn_join_room);

        btnJoinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinRoom();
            }
        });

        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMatchIfHost();
            }
        });
    }

    private void createRoom() {
        setLoading(true);
        updateStatus("匹配状态：正在创建房间...");
        sessionManager.createRoom(playerId, new MultiplayerSessionManager.RoomCallback() {
            @Override
            public void onSuccess(String createdRoomId) {
                setLoading(false);
                roomId = createdRoomId;
                isHost = true;
                isMatched = false;
                gameStarted = false;
                tvRoomCode.setText(roomId);
                updateStatus("匹配状态：房间已创建，等待对手加入");
                btnStartGame.setEnabled(false);
                startPollingRoomStatus();
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);
                Toast.makeText(OnlineLobbyActivity.this, "创建失败：" + error, Toast.LENGTH_SHORT).show();
                updateStatus("匹配状态：创建房间失败");
            }
        });
    }

    private void joinRoom() {
        String input = etRoomCode.getText().toString().trim();
        if (TextUtils.isEmpty(input) || input.length() != 6) {
            Toast.makeText(this, "请输入六位房间号", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        updateStatus("匹配状态：正在加入房间...");
        sessionManager.joinRoom(input, playerId, new MultiplayerSessionManager.JoinCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                roomId = input;
                isHost = false;
                isMatched = false;
                gameStarted = false;
                tvRoomCode.setText(roomId);
                updateStatus("匹配状态：已加入，等待房主开始");
                btnStartGame.setEnabled(false);
                startPollingRoomStatus();
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);
                Toast.makeText(OnlineLobbyActivity.this, "加入失败：" + error, Toast.LENGTH_SHORT).show();
                updateStatus("匹配状态：加入房间失败");
            }
        });
    }

    private void startMatchIfHost() {
        if (!isHost || !isMatched || roomId == null) {
            Toast.makeText(this, "仅房主在匹配成功后可开始", Toast.LENGTH_SHORT).show();
            return;
        }

        btnStartGame.setEnabled(false);
        updateStatus("匹配状态：房主正在开始游戏...");
        sessionManager.startMatch(roomId, playerId, new MultiplayerSessionManager.SimpleCallback() {
            @Override
            public void onSuccess() {
                enterMultiplayerGame();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(OnlineLobbyActivity.this, "开始失败：" + error, Toast.LENGTH_SHORT).show();
                updateStatus("匹配状态：开始游戏失败");
                btnStartGame.setEnabled(isMatched && isHost);
            }
        });
    }

    private void startPollingRoomStatus() {
        stopPollingRoomStatus();
        roomStatusPollTask = new Runnable() {
            @Override
            public void run() {
                if (roomId == null || gameStarted) {
                    return;
                }
                sessionManager.getRoomStatus(roomId, playerId, new MultiplayerSessionManager.RoomStatusCallback() {
                    @Override
                    public void onSuccess(MultiplayerSessionManager.RoomStatusResponse response) {
                        isMatched = response.matched;
                        if (isMatched) {
                            updateStatus("匹配状态：匹配成功");
                        } else {
                            updateStatus("匹配状态：等待匹配中...");
                        }
                        btnStartGame.setEnabled(isMatched && isHost);

                        if (response.started) {
                            enterMultiplayerGame();
                            return;
                        }
                        handler.postDelayed(roomStatusPollTask, 1200);
                    }

                    @Override
                    public void onFailure(String error) {
                        updateStatus("匹配状态：状态同步失败，重试中");
                        handler.postDelayed(roomStatusPollTask, 2000);
                    }
                });
            }
        };
        handler.post(roomStatusPollTask);
    }

    private void stopPollingRoomStatus() {
        if (roomStatusPollTask != null) {
            handler.removeCallbacks(roomStatusPollTask);
        }
    }

    private void enterMultiplayerGame() {
        if (gameStarted) {
            return;
        }
        gameStarted = true;
        stopPollingRoomStatus();

        Intent intent = new Intent(OnlineLobbyActivity.this, MainActivity.class);
        intent.putExtra("game_mode", gameMode);
        intent.putExtra("is_multiplayer", true);
        intent.putExtra("room_id", roomId);
        intent.putExtra("player_id", playerId);
        intent.putExtra("is_host", isHost);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void setLoading(boolean loading) {
        progressMatch.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void updateStatus(String statusText) {
        tvMatchStatus.setText(statusText);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPollingRoomStatus();
    }
}
