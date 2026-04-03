package com.example.aircraftwardemo.manager;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import com.example.aircraftwardemo.R;

import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private static AudioManager instance;

    // 背景音乐（普通）
    private MediaPlayer bgmPlayer;
    // Boss 战背景音乐
    private MediaPlayer bossBgmPlayer;
    // 音效池
    private SoundPool soundPool;
    private Map<Integer, Integer> soundMap;

    // 全局开关
    private boolean isMusicOn = true;
    private boolean isSoundOn = true;
    private boolean isInitialized = false;
    private boolean isBossBgmPlaying = false;

    private AudioManager() {}

    // 单例模式
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void init(Context context) {
        if (isInitialized) {
            return;  // 防止重复初始化
        }
        initMusic(context);
        initSounds(context);
        isInitialized = true;
    }

    // 1. 初始化背景音乐
    private void initMusic(Context context) {
        bgmPlayer = MediaPlayer.create(context, R.raw.bgm);
        if (bgmPlayer != null) {
            bgmPlayer.setLooping(true); // 循环播放
            bgmPlayer.setVolume(1f, 1f); // 音量大小
        }

        // Boss 背景音乐（单独 MediaPlayer）
        bossBgmPlayer = MediaPlayer.create(context, R.raw.bgm_boss);
        if (bossBgmPlayer != null) {
            bossBgmPlayer.setLooping(true);
            bossBgmPlayer.setVolume(1.1f, 1.1f);
        }
    }

    // 2. 初始化所有音效
    private void initSounds(Context context) {
        soundMap = new HashMap<>();

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(5)
                    .build();
        } else {
            soundPool = new SoundPool(5, android.media.AudioManager.STREAM_MUSIC, 0);
        }

        // 👇 把你所有的音效都在这里加载
        soundMap.put(R.raw.bomb_explosion, soundPool.load(context, R.raw.bomb_explosion, 1));
        soundMap.put(R.raw.bullet, soundPool.load(context, R.raw.bullet, 1));
        soundMap.put(R.raw.bullet_hit, soundPool.load(context, R.raw.bullet_hit, 1));
        soundMap.put(R.raw.game_over, soundPool.load(context, R.raw.game_over, 1));
        soundMap.put(R.raw.get_supply, soundPool.load(context, R.raw.get_supply, 1));
    }

    // 普通 BGM 从头播放（适合每次进入游戏或返回主菜单）
    public void restartBGM() {
        if (!isMusicOn) return;
        if (bgmPlayer != null) {
            // 如果正在播放，先停止并重置位置
            if (bgmPlayer.isPlaying()) {
                bgmPlayer.pause();
            }
            bgmPlayer.seekTo(0);
            bgmPlayer.start();
        }
        // 如果有 Boss BGM 正在播放，也停止并重置（保证回到主菜单时是普通 BGM）
        if (bossBgmPlayer != null && bossBgmPlayer.isPlaying()) {
            bossBgmPlayer.pause();
            bossBgmPlayer.seekTo(0);
        }
        isBossBgmPlaying = false;
    }


    // --- 普通背景音乐控制 ---
    public void playBGM() {
        if (isMusicOn && bgmPlayer != null && !bgmPlayer.isPlaying() && !isBossBgmPlaying) {
            bgmPlayer.start();
        }
    }
    public void pauseBGM() {
        if (bgmPlayer != null && bgmPlayer.isPlaying()) {
            bgmPlayer.pause();
        }
    }

    // --- Boss 背景音乐控制 ---
    public void playBossBGM() {
        if (!isMusicOn) return;
        // 先暂停普通 BGM
        if (bgmPlayer != null && bgmPlayer.isPlaying()) {
            bgmPlayer.pause();
        }
        // 播放 Boss BGM
        if (bossBgmPlayer != null && !bossBgmPlayer.isPlaying()) {
            bossBgmPlayer.start();
            isBossBgmPlaying = true;
        }
    }
    public void stopBossBGM() {
        if (bossBgmPlayer != null && bossBgmPlayer.isPlaying()) {
            bossBgmPlayer.pause();
            bossBgmPlayer.seekTo(0); // 重置到开头，方便下次播放
        }
        isBossBgmPlaying = false;
        // 恢复普通 BGM
        if (isMusicOn && bgmPlayer != null && !bgmPlayer.isPlaying()) {
            bgmPlayer.start();
        }
    }


    // --- 音效 ---
    public void playSound(int soundId) {
        if (isSoundOn && soundPool != null && soundMap.containsKey(soundId)) {
            // 参数：leftVolume, rightVolume, priority, loop, rate
            soundPool.play(soundMap.get(soundId), 1.5f, 1.5f, 1, 0, 1.0f);
        }
    }

    // --- 开关控制 ---
    public void setMusicOn(boolean on) {
        isMusicOn = on;
        if (on) {
            if (!isBossBgmPlaying) {
                playBGM();
            } else {
                // 如果当前是 Boss 战，恢复 Boss BGM
                if (bossBgmPlayer != null && !bossBgmPlayer.isPlaying()) {
                    bossBgmPlayer.start();
                }
            }
        } else {
            pauseBGM();
            if (bossBgmPlayer != null && bossBgmPlayer.isPlaying()) {
                bossBgmPlayer.pause();
            }
        }
    }

    public void setSoundOn(boolean on) {
        isSoundOn = on;
    }

    public boolean isMusicOn() {
        return isMusicOn;
    }

    // --- 资源释放 ---
    public void release() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.release();
            bgmPlayer = null;
        }
        if (bossBgmPlayer != null) {
            bossBgmPlayer.stop();
            bossBgmPlayer.release();
            bossBgmPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (soundMap != null) {
            soundMap.clear();
        }
        isInitialized = false;
    }
}
