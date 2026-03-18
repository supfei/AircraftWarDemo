package com.example.aircraftwardemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private RadioButton radioOn, radioOff;
    private Button btnEasy, btnNormal, btnHard;
    private boolean isMusicOn = true; // 默认音乐开启

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // 初始化所有UI元素
        initViews();

        // 设置点击事件
        setupListeners();
    }

    private void initViews() {
        radioGroup = findViewById(R.id.radio_group);
        radioOn = findViewById(R.id.radio_on);
        radioOff = findViewById(R.id.radio_off);
        btnEasy = findViewById(R.id.btn_easy);
        btnNormal = findViewById(R.id.btn_normal);
        btnHard = findViewById(R.id.btn_hard);
    }

    private void setupListeners() {
        // 音乐开关监听
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_on) {
                    isMusicOn = true;
                    Toast.makeText(MainMenuActivity.this, "音乐已开启", Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.radio_off) {
                    isMusicOn = false;
                    Toast.makeText(MainMenuActivity.this, "音乐已关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 简单模式按钮
        btnEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame("easy");
            }
        });

        // 普通模式按钮
        btnNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame("normal");
            }
        });

        // 困难模式按钮
        btnHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame("hard");
            }
        });
    }

    private void startGame(String mode) {
        Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
        intent.putExtra("game_mode", mode);
        intent.putExtra("music_on", isMusicOn);
        startActivity(intent);
        // 添加切换动画
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
