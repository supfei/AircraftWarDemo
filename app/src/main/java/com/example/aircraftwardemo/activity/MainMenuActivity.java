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
    private Button btnSingle, btnOnline;
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
        btnSingle = findViewById(R.id.btn_single);
        btnOnline = findViewById(R.id.btn_online);
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

        // 单机模式按钮
        btnSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDifficultySelect(false);
            }
        });

        // 联机模式按钮
        btnOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDifficultySelect(true);
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
    private void startDifficultySelect(boolean fromOnlineMode) {
        Intent intent = new Intent(MainMenuActivity.this, DifficultySelectActivity.class);
        intent.putExtra("from_online_mode", fromOnlineMode);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // 添加方法
    private void startRankingActivity() {
        Intent intent = new Intent(MainMenuActivity.this, GlobalRankingActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
