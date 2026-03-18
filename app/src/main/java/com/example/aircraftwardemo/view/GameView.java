// GameView.java
package com.example.aircraftwardemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.aircraftwardemo.controller.GameConfig;
import com.example.aircraftwardemo.controller.EasyGameController;
import com.example.aircraftwardemo.controller.GameController;
import com.example.aircraftwardemo.controller.HardGameController;
import com.example.aircraftwardemo.controller.NormalGameController;
import com.example.aircraftwardemo.manager.ImageManager;
import com.example.aircraftwardemo.model.HeroAircraft;

/**
 * 游戏视图，继承SurfaceView
 * 负责：游戏循环、渲染、输入处理
 */
public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    // 游戏线程
    private Thread gameThread;
    private volatile boolean isRunning = false;

    // Surface管理
    private SurfaceHolder holder;

    // 游戏控制器
    private GameController gameController;
    private String difficulty = "hard";

    // 屏幕尺寸
    private int screenWidth;
    private int screenHeight;

    // 性能统计
    private long lastFpsTime = 0;
    private int fps = 0;

    /**
     * 代码中创建View时调用
     */
    public GameView(Context context, String difficulty) {
        super(context);
        this.difficulty = difficulty;
        initView(context);
    }

    /**
     * XML布局中创建View时调用
     */
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        // 1. 获取SurfaceHolder并注册回调
        holder = getHolder();
        holder.addCallback(this);

        // 2. 设置View属性
        setFocusable(true);                    // 允许接收焦点
        setFocusableInTouchMode(true);         // 允许在触摸模式下接收焦点
        setKeepScreenOn(true);                 // 保持屏幕常亮

        // 3. 初始化图片管理器
        ImageManager.init(context);

        // 4. 获取屏幕尺寸
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        // 5. 设置全局游戏配置
        GameConfig.setScreenSize(screenWidth, screenHeight);

        Log.d("GameView", "屏幕尺寸: " + screenWidth + "x" + screenHeight);
    }

    // ========== Surface生命周期回调 ==========

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GameView", "Surface创建");

        // 1. 初始化游戏控制器
        initGameController();

        // 2. 设置英雄机位置（不需要重置实例）
        HeroAircraft hero = HeroAircraft.getInstance();

        // 设置英雄机的GameController引用
        if (gameController != null) {
            hero.setGame(gameController);
        }

        // 设置英雄机初始位置
        int heroX = screenWidth / 2;
        int heroY = screenHeight - 100; // 离底部100像素

        // 如果图片已加载，可以基于图片高度计算位置
        if (ImageManager.HERO_IMAGE != null) {
            heroY = screenHeight - ImageManager.HERO_IMAGE.getHeight() / 2;
        }

        hero.setLocation(heroX, heroY);

        // 3. 启动游戏线程
        isRunning = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("GameView", "Surface尺寸变化: " + width + "x" + height);

        // 更新屏幕尺寸
        this.screenWidth = width;
        this.screenHeight = height;
        GameConfig.setScreenSize(width, height);

        // 通知游戏控制器
        if (gameController != null) {
            gameController.setScreenSize(width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GameView", "Surface销毁");

        // 停止游戏线程
        isRunning = false;
        boolean retry = true;

        while (retry) {
            try {
                gameThread.join();  // 等待线程结束
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 清理资源
        if (gameController != null) {
            gameController.cleanup();
        }
    }

    // ========== 游戏主循环 ==========

    @Override
    public void run() {
        Log.d("GameView", "游戏线程启动");

        long lastTime = System.nanoTime();
        double nsPerUpdate = 1000000000.0 / 60.0; // 目标60FPS
        double delta = 0;
        long timer = System.currentTimeMillis();
        int updates = 0;
        int frames = 0;

        // 游戏主循环
        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerUpdate;
            lastTime = now;

            // 固定时间步长更新
            while (delta >= 1) {
                updateGame();
                updates++;
                delta--;
            }

            // 渲染游戏
            renderGame();
            frames++;

            // 控制帧率
            try {
                long frameTime = (System.nanoTime() - now) / 1000000;
                long sleepTime = Math.max(0, 16 - frameTime); // 16ms ≈ 60FPS
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 每秒输出一次FPS
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                Log.d("GameView", "FPS: " + frames + " | Updates: " + updates);
                frames = 0;
                updates = 0;
            }
        }

        Log.d("GameView", "游戏线程结束");
    }

    private void updateGame() {
        if (gameController != null && !gameController.isGameOver()) {
            gameController.update(16); // 假设16ms一帧
        }
    }

    private void renderGame() {
        // 检查Surface是否有效
        if (!holder.getSurface().isValid()) {
            return;
        }

        Canvas canvas = null;
        try {
            // 锁定画布开始绘制
            canvas = holder.lockCanvas();

            if (canvas != null) {
                synchronized (holder) {
                    // 清空画布
                    canvas.drawColor(android.graphics.Color.BLACK);

                    // 绘制游戏
                    if (gameController != null) {
                        gameController.draw(canvas);
                    }
                }
            }
        } finally {
            // 解锁画布并提交
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    // ========== 触摸事件处理 ==========

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // 获取触摸位置
                float x = event.getX();
                float y = event.getY();

                // 传递给游戏控制器
                if (gameController != null && !gameController.isGameOver()) {
                    gameController.handleTouch((int)x, (int)y);
                }
                return true;

            case MotionEvent.ACTION_UP:
                // 手指抬起
                return true;
        }

        return super.onTouchEvent(event);
    }

    // ========== 辅助方法 ==========

    private void initGameController() {
        // 根据难度创建不同的游戏控制器
        switch (difficulty) {
            case "easy":
                gameController = new EasyGameController(false, getContext());
                break;
            case "normal":
                gameController = new NormalGameController(false, getContext());
                break;
            case "hard":
                gameController = new HardGameController(false, getContext());
                break;
            default:
                gameController = new EasyGameController(false, getContext());
        }

        // 设置屏幕尺寸
        if (gameController != null) {
            gameController.setScreenSize(screenWidth, screenHeight);
        }
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public GameController getGameController() {
        return gameController;
    }
}