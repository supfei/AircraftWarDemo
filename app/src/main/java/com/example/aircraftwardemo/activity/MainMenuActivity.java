package com.example.aircraftwardemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aircraftwardemo.R;
import com.example.aircraftwardemo.manager.AudioManager;

public class MainMenuActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private RadioButton radioOn, radioOff;
    private Button btnEasy, btnNormal, btnHard;
    private boolean isMusicOn = true; // 默认音乐开启

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // 初始化音频
        AudioManager.getInstance().init(this);

        // 初始化所有UI元素
        initViews();

        // 设置点击事件
        setupListeners();

        // 根据当前开关状态自动播放音乐
        AudioManager.getInstance().restartBGM();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从游戏或其他页面返回时，也从头播放音乐
        AudioManager.getInstance().restartBGM();
    }

    private void initViews() {
        radioGroup = findViewById(R.id.radio_group);
        radioOn = findViewById(R.id.radio_on);
        radioOff = findViewById(R.id.radio_off);
        btnEasy = findViewById(R.id.btn_easy);
        btnNormal = findViewById(R.id.btn_normal);
        btnHard = findViewById(R.id.btn_hard);
        radioOn.setChecked(true);
    }

    private void setupListeners() {
        // 音乐开关监听
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_on) {
                    isMusicOn = true;
                    AudioManager.getInstance().setMusicOn(true);
                    Toast.makeText(MainMenuActivity.this, "音乐已开启", Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.radio_off) {
                    isMusicOn = false;
                    AudioManager.getInstance().setMusicOn(false);
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

        Button btnRanking = findViewById(R.id.btn_ranking);
        btnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRankingActivity();
            }
        });

    }
    // 添加方法
    private void startRankingActivity() {
        Intent intent = new Intent(MainMenuActivity.this, GlobalRankingActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
