package com.example.aircraftwardemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aircraftwardemo.R;
import com.example.aircraftwardemo.data.ScoreRecord;
import com.example.aircraftwardemo.network.ScoreRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GameOverActivity extends AppCompatActivity {

    public static final String EXTRA_FINAL_SCORE = "final_score";
    public static final String EXTRA_GAME_MODE = "game_mode";
    public static final String EXTRA_IS_MULTIPLAYER = "is_multiplayer";

    private int finalScore;
    private String gameMode;
    private boolean isMultiplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        finalScore = getIntent().getIntExtra(EXTRA_FINAL_SCORE, 0);
        gameMode = getIntent().getStringExtra(EXTRA_GAME_MODE);
        isMultiplayer = getIntent().getBooleanExtra(EXTRA_IS_MULTIPLAYER, false);
        if (gameMode == null) {
            gameMode = "easy";
        }

        TextView tvFinalScore = findViewById(R.id.tv_final_score);
        TextView tvHighScore = findViewById(R.id.tv_high_score);
        EditText etPlayerName = findViewById(R.id.et_player_name);
        Button btnSaveRestart = findViewById(R.id.btn_save_restart);
        Button btnBackMenu = findViewById(R.id.btn_back_menu);

        tvFinalScore.setText("最终得分: " + finalScore);

        ScoreRepository repository = ScoreRepository.getInstance(this);
        List<ScoreRecord> scores = repository.getLocalScores();
        int highScore = scores.isEmpty() ? 0 : scores.get(0).getScore();
        tvHighScore.setText("历史最高: " + highScore);

        btnSaveRestart.setOnClickListener(v -> {
            String playerName = etPlayerName.getText().toString().trim();
            if (playerName.isEmpty()) {
                playerName = "匿名玩家";
            }

            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            ScoreRecord scoreRecord = new ScoreRecord(playerName, finalScore, currentTime);
            repository.addScore(scoreRecord);
            backToMainMenu();
        });

        btnBackMenu.setOnClickListener(v -> backToMainMenu());
    }

    private void backToMainMenu() {
        Intent intent = new Intent(GameOverActivity.this, MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
