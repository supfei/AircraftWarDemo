package com.example.aircraftwardemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aircraftwardemo.R;

public class DifficultySelectActivity extends AppCompatActivity {
    private boolean fromOnlineMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty_select);
        fromOnlineMode = getIntent().getBooleanExtra("from_online_mode", false);

        Button btnEasy = findViewById(R.id.btn_easy);
        Button btnNormal = findViewById(R.id.btn_normal);
        Button btnHard = findViewById(R.id.btn_hard);
        TextView tvSubtitle = findViewById(R.id.tv_subtitle);
        if (fromOnlineMode) {
            tvSubtitle.setText("请选择联机难度");
        } else {
            tvSubtitle.setText("请选择单机难度");
        }

        btnEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDifficultyChosen("easy");
            }
        });

        btnNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDifficultyChosen("normal");
            }
        });

        btnHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDifficultyChosen("hard");
            }
        });
    }

    private void onDifficultyChosen(String mode) {
        Intent intent;
        if (fromOnlineMode) {
            intent = new Intent(DifficultySelectActivity.this, OnlineLobbyActivity.class);
            intent.putExtra("game_mode", mode);
        } else {
            intent = new Intent(DifficultySelectActivity.this, MainActivity.class);
            intent.putExtra("game_mode", mode);
            intent.putExtra("is_multiplayer", false);
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
