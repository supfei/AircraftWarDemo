// GameView.java
package com.example.aircraftwardemo.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.aircraftwardemo.R;
import com.example.aircraftwardemo.controller.GameConfig;
import com.example.aircraftwardemo.controller.EasyGameController;
import com.example.aircraftwardemo.controller.GameController;
import com.example.aircraftwardemo.controller.HardGameController;
import com.example.aircraftwardemo.controller.NormalGameController;
import com.example.aircraftwardemo.data.ScoreRecord;
import com.example.aircraftwardemo.factory.GameControllerFactory;
import com.example.aircraftwardemo.manager.ImageManager;
import com.example.aircraftwardemo.model.HeroAircraft;
import com.example.aircraftwardemo.network.ScoreRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 游戏视图，继承SurfaceView
 * 负责：游戏循环、渲染、输入处理
 */
public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback,GameController.GameOverListener {


    // 添加接口定义
    public interface GameOverCallback {
        void onGameOver(int finalScore);
    }

    private boolean isGameOverShown = false;
    private GameOverCallback gameOverCallback;

    // 在类中添加
    public void setGameOverCallback(GameOverCallback callback) {
        this.gameOverCallback = callback;
    }

    // 游戏线程
    private Thread gameThread;
    private volatile boolean isRunning = false;

    // Surface管理
    private SurfaceHolder holder;

    // 游戏控制器
    private GameController gameController;
    private String difficulty = "hard";
    private boolean soundEnabled = false;
    private boolean multiplayerEnabled = false;
    private int enemyScore = -1;

    // 屏幕尺寸
    private int screenWidth;
    private int screenHeight;

    // 性能统计
    private long lastFpsTime = 0;
    private int fps = 0;

    // 清理
    private volatile boolean isCleanedUp = false;

    /**
     * 代码中创建View时调用
     */
    public GameView(Context context, String difficulty, boolean soundEnabled) {
        super(context);
        this.difficulty = difficulty;
        this.soundEnabled = soundEnabled;
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

        // 2. 设置英雄机位置
        HeroAircraft hero = HeroAircraft.getInstance();
        hero.reset();

        // 设置监听器
        if (gameController != null) {
            gameController.setGameOverListener(this);
        }

        // 设置英雄机的GameController引用
        if (gameController != null) {
            hero.setGame(gameController);
        }

        // 3. 启动游戏线程（【修复点】：增加防重复启动的校验）
        if (gameThread == null || !gameThread.isAlive()) {
            isRunning = true;
            gameThread = new Thread(this, "GameThread");
            gameThread.start();
        }
    }

    // 实现GameController.GameOverListener接口的方法
    @Override
    public void onGameOver(int finalScore) {
        runOnUiThreadSafely(() -> showGameOverDialog(finalScore));
    }

    // 显示游戏结束对话框
    private void showGameOverDialog(int finalScore) {
        if (isGameOverShown || getContext() == null) {
            Log.e("GameView", "Context is null, cannot show game over dialog");
            return;
        }

        isGameOverShown = true;

        Activity activity = getActivityFromContext();
        if (activity == null) {
            Log.e("GameView", "Activity is null, cannot show game over dialog");
            isGameOverShown = false;
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.game_over_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        // 初始化视图组件
        TextView tvFinalScore = dialogView.findViewById(R.id.tv_final_score);
        TextView tvHighScore = dialogView.findViewById(R.id.tv_high_score);
        EditText etPlayerName = dialogView.findViewById(R.id.et_player_name);
        Button btnSaveRestart = dialogView.findViewById(R.id.btn_save_restart);
        Button btnBackMenu = dialogView.findViewById(R.id.btn_back_menu);

        // 设置分数
        tvFinalScore.setText("最终得分: " + finalScore);

        // 获取并显示历史最高分
        ScoreRepository repository = ScoreRepository.getInstance(getContext());
        List<ScoreRecord> scores = repository.getLocalScores();
        int highScore = scores.isEmpty() ? 0 : scores.get(0).getScore();
        tvHighScore.setText("历史最高: " + highScore);

        AlertDialog dialog = builder.create();
        dialog.show();

        // 设置窗口背景透明
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 按钮事件监听
        btnSaveRestart.setOnClickListener(v -> {
            String playerName = etPlayerName.getText().toString().trim();
            if (playerName.isEmpty()) {
                playerName = "匿名玩家";
            }

            // 保存分数
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            ScoreRecord scoreRecord = new ScoreRecord(playerName, finalScore, currentTime);
            repository.addScore(scoreRecord);

            // 重新开始游戏
            dialog.dismiss();
            returnToMainMenu();
        });

        btnBackMenu.setOnClickListener(v -> {
            dialog.dismiss();
            returnToMainMenu();
        });

        // 对话框关闭时重置标志
        dialog.setOnDismissListener(dialog1 -> {
            isGameOverShown = false;
        });
    }

    // 返回主菜单
    private void returnToMainMenu() {
        requestStopGameThread();

        // 清理游戏资源
        if (gameController != null) {
            gameController.cleanup();
        }

        // 结束当前Activity，返回主菜单
        Activity activity = getActivityFromContext();
        if (activity != null) {
            activity.finish();
        }
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

        requestStopGameThread();

        // 清理资源
        cleanupGameController();

        Log.d("GameView", "Surface销毁完成");
    }

    // ========== 游戏主循环 ==========

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerUpdate = 1000000000.0 / 60.0;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int updates = 0;
        int frames = 0;

        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerUpdate;
            lastTime = now;

            // 限制最大追赶步数，防止死亡螺旋（最多连续更新6帧）
            int updateCount = 0;
            while (delta >= 1 && updateCount < 6) {
                updateGame();
                updates++;
                delta--;
                updateCount++;
            }

            renderGame();
            frames++;

            // 精准睡眠控制
            try {
                long executionTimeNs = System.nanoTime() - now;
                long sleepTimeMs = (long) ((1000000000.0 / 60.0 - executionTimeNs) / 1000000);
                if (sleepTimeMs > 0) {
                    Thread.sleep(sleepTimeMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

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

    private final Object surfaceLock = new Object();

    private void renderGame() {
        // 检查Surface是否有效
        if (holder == null || holder.getSurface() == null || !holder.getSurface().isValid()) {
            return;
        }

        Canvas canvas = null;
        try {
            // 使用同步锁确保线程安全
            synchronized (surfaceLock) {
                canvas = holder.lockCanvas();

                if (canvas != null) {
                    // 清空画布
                    canvas.drawColor(Color.BLACK);

                    // 绘制游戏
                    if (gameController != null) {
                        gameController.draw(canvas);
                    }
                }
            }
        } finally {
            // 解锁画布并提交
            if (canvas != null) {
                try {
                    holder.unlockCanvasAndPost(canvas);
                } catch (Exception e) {
                    Log.e("GameView", "解锁画布时出错: " + e.getMessage());
                    // 忽略错误，可能是Surface已销毁
                }
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
//        // 根据难度创建不同的游戏控制器
//        switch (difficulty) {
//            case "easy":
//                gameController = new EasyGameController(false, getContext());
//                break;
//            case "normal":
//                gameController = new NormalGameController(false, getContext());
//                break;
//            case "hard":
//                gameController = new HardGameController(false, getContext());
//                break;
//            default:
//                gameController = new EasyGameController(false, getContext());
//        }
        gameController = GameControllerFactory.createController(difficulty, soundEnabled, getContext());

        if (gameController != null) {
            gameController.setGameOverListener(this);
            HeroAircraft.getInstance().setGame(gameController);
            gameController.setMultiplayerEnabled(multiplayerEnabled);
            gameController.setEnemyScore(enemyScore);
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

    public void setMultiplayerEnabled(boolean multiplayerEnabled) {
        this.multiplayerEnabled = multiplayerEnabled;
        if (gameController != null) {
            gameController.setMultiplayerEnabled(multiplayerEnabled);
        }
    }

    public void setEnemyScore(int enemyScore) {
        this.enemyScore = enemyScore;
        if (gameController != null) {
            gameController.setEnemyScore(enemyScore);
        }
    }

    public int getCurrentScore() {
        if (gameController == null) {
            return 0;
        }
        return gameController.getScore();
    }

    /**
     * 清理GameView的所有资源
     */
    public void cleanup() {
        if (isCleanedUp) {
            return; // 防止重复清理
        }

        isCleanedUp = true;
        Log.d("GameView", "开始清理GameView资源");

        // 1. 停止游戏线程
        stopGameThread();

        // 2. 清理游戏控制器
        cleanupGameController();

        // 3. 移除SurfaceHolder回调
        removeSurfaceHolderCallback();

        // 4. 清理图片资源（可选）
        cleanupImageResources();

        // 5. 移除触摸监听
        setOnTouchListener(null);

        Log.d("GameView", "GameView清理完成");
    }

    /**
     * 安全停止游戏线程
     */
    private void stopGameThread() {
        Log.d("GameView", "停止游戏线程...");

        requestStopGameThread();
        Thread threadToWait = gameThread;
        if (threadToWait != null) {
            try {
                // 等待线程结束
                threadToWait.join(1000); // 最多等待1秒
                if (threadToWait.isAlive()) {
                    Log.w("GameView", "游戏线程仍在运行，尝试中断");
                    threadToWait.interrupt();
                }
            } catch (InterruptedException e) {
                Log.e("GameView", "等待游戏线程结束时中断", e);
                Thread.currentThread().interrupt();
            } finally {
                gameThread = null;
            }
        }

        Log.d("GameView", "游戏线程已停止");
    }

    /**
     * 清理游戏控制器
     */
    private void cleanupGameController() {
        Log.d("GameView", "清理游戏控制器...");

        if (gameController != null) {
            try {
                gameController.cleanup();
            } catch (Exception e) {
                Log.e("GameView", "清理游戏控制器时出错", e);
            } finally {
                gameController = null;
            }
        }

        Log.d("GameView", "游戏控制器已清理");
    }

    /**
     * 移除SurfaceHolder回调
     */
    private void removeSurfaceHolderCallback() {
        Log.d("GameView", "移除SurfaceHolder回调...");

        if (holder != null) {
            try {
                holder.removeCallback(this);
            } catch (Exception e) {
                Log.e("GameView", "移除SurfaceHolder回调时出错", e);
            }
        }

        Log.d("GameView", "SurfaceHolder回调已移除");
    }

    /**
     * 清理图片资源（根据您的需求决定是否清理）
     */
    private void cleanupImageResources() {
        Log.d("GameView", "清理图片资源...");

        // 注意：ImageManager通常是全局的，如果其他GameView实例还需要使用，不要清理
        // 这里只是一个示例，您可以根据需要调整
        if (isCleanedUp && getContext() != null) {
            try {
                // 可以清理一些临时的位图资源
                // 但通常ImageManager会自己管理资源生命周期
            } catch (Exception e) {
                Log.e("GameView", "清理图片资源时出错", e);
            }
        }

        Log.d("GameView", "图片资源已清理");
    }

    /**
     * 重新启动游戏（用于从暂停状态恢复）
     */
    public void resumeGame() {
        if (isCleanedUp) {
            Log.d("GameView", "GameView已清理，无法恢复");
            return;
        }

        // 【修复点】：增加 isValid() 和线程存活状态的校验
        if (!isRunning && holder != null && holder.getSurface() != null && holder.getSurface().isValid()) {
            Log.d("GameView", "恢复游戏");
            if (gameThread == null || !gameThread.isAlive()) {
                isRunning = true;
                gameThread = new Thread(this, "GameThread");
                gameThread.start();
            }
        }
    }

    /**
     * 暂停游戏
     */
    public void pauseGame() {
        Log.d("GameView", "暂停游戏");
        isRunning = false;
    }

    private void requestStopGameThread() {
        isRunning = false;
        Thread thread = gameThread;
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    private Activity getActivityFromContext() {
        Context context = getContext();
        if (context instanceof Activity) {
            return (Activity) context;
        }
        return null;
    }

    private void runOnUiThreadSafely(Runnable action) {
        Activity activity = getActivityFromContext();
        if (activity != null) {
            activity.runOnUiThread(action);
        } else {
            post(action);
        }
    }



}