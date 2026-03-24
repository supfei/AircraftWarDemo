package com.example.aircraftwardemo.controller;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

import com.example.aircraftwardemo.data.ScoreRecord;
import com.example.aircraftwardemo.factory.*;
import com.example.aircraftwardemo.manager.*;
import com.example.aircraftwardemo.model.*;
import com.example.aircraftwardemo.network.ScoreRepository;
import com.example.aircraftwardemo.observer.Observer;
import com.example.aircraftwardemo.pool.BulletPool;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class GameController {
//    新：context
    private Context context; //获取android的系统资源
    protected boolean soundEnabled;
    protected EnemySpawnManager enemySpawnManager;

    // 敌机配置
    EnemyConfig mobConfig = new EnemyConfig(0, 3, 1, 10, 0);       // speedX, speedY, hp, score, propDropCount
    EnemyConfig eliteConfig = new EnemyConfig(3, 2, 5, 30, 1);
    EnemyConfig plusConfig = new EnemyConfig(2, 2, 5, 45, 2);
    EnemyConfig bossConfig = new EnemyConfig(1, 1, 200, 200, 3);

    // Boss 触发分数：200 分
    protected  int BOSS_SCORE_THRESHOLD = 200;

    // ========== 游戏对象 ==========
    protected HeroAircraft heroAircraft;
    protected List<EnemyAircraft> enemyAircrafts;
    protected List<BaseBullet> heroBullets;
    protected List<BaseBullet> enemyBullets;
    protected List<AbstractProp> allProps;

    // ========== 游戏状态 ==========
    protected int score = 0;
    protected int time = 0;
    protected int timeInterval = 40; // 刷新间隔（毫秒）
    protected boolean gameOverFlag = false;
    protected ScheduledExecutorService executorService;
//    protected MusicManager musicManager;
//声音还没做

    // ========== 行为调度器（定时触发器） ==========
    // 分别用于控制：敌机生成、敌机射击、英雄射击的周期性触发
    protected BehaviorScheduler enemySpawnScheduler;   // 敌机生成
    protected BehaviorScheduler enemyShootScheduler;   // 敌机射击
    protected BehaviorScheduler heroShootScheduler;    // 英雄射击


    // ========== 游戏配置（可由子类覆盖）=========
    protected int enemyMaxNumber = 7; // 敌机最大数量，子类可覆写 getEnemyMaxNumber()

    // 新增：游戏界面相关
    protected int screenWidth;
    protected int screenHeight;

    // 画笔对象（用于绘制）
    protected Paint paint = new Paint();
    // 新增：触摸控制器
    protected TouchController touchController;

    public GameController (boolean soundEnabled, Context context) {
//新：传context
        this.context = context;

        // 初始化游戏对象
        this.heroAircraft = HeroAircraft.getInstance();
        this.heroAircraft.reset();
        this.heroAircraft.setGame(this);
        // 初始化触摸控制器
        this.touchController = new TouchController(heroAircraft);

        this.enemyAircrafts = new LinkedList<>();
        this.heroBullets = new LinkedList<>();
        this.enemyBullets = new LinkedList<>();
        this.allProps = new LinkedList<>();
        this.soundEnabled = soundEnabled;

        // 获取屏幕尺寸
        this.screenWidth = GameConfig.getScreenWidth();
        this.screenHeight = GameConfig.getScreenHeight();

        // 直接在构造函数中初始化paint
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setFilterBitmap(true);


        this.executorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            private int threadNumber = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "game-action-" + threadNumber++);
                thread.setDaemon(true);
                return thread;
            }
        });

//        // 启动英雄机控制（如鼠标监听）
//        new HeroController(this, heroAircraft);
        enemySpawnManager = new EnemySpawnManager(
                mobConfig, eliteConfig, plusConfig, bossConfig,
                100, 0, 0, BOSS_SCORE_THRESHOLD,
                this
        );

        initSchedulers(); // 初始化调度器
    }


    /**
     * 更新游戏状态
     * @param deltaTime 距离上一帧的时间（毫秒）
     */
    public void update(int deltaTime) {
        if (gameOverFlag) return;

        time += deltaTime;

        // 时间相关逻辑
        onTimeIncrease();

        // 更新调度器
        enemySpawnScheduler.update(deltaTime);
        enemyShootScheduler.update(deltaTime);
        heroShootScheduler.update(deltaTime);

        // 移动所有对象
        moveActions();

        // 碰撞检测
        crashChecks();

        // 清理无效对象
        postProcesses();

        // 检查游戏结束
        checkGameOver();
    }


    /**
     * 绘制游戏到Canvas
     */
    public void draw(Canvas canvas) {
        if (canvas == null) return;

        // 1. 绘制背景
        drawBackground(canvas);

        // 2. 绘制所有游戏对象
        drawFlyingObjects(canvas, enemyBullets);
        drawFlyingObjects(canvas, heroBullets);
        drawFlyingObjects(canvas, enemyAircrafts);
        drawFlyingObjects(canvas, allProps);

        // 3. 绘制英雄机
        drawHeroAircraft(canvas);

        // 4. 绘制UI（分数、生命值）
        drawUI(canvas);
    }

    // 新增：获取触摸控制器的方法
    public TouchController getTouchController() {
        return touchController;
    }

    // 新增：设置屏幕尺寸（同时更新触摸控制器）
    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        GameConfig.setScreenSize(width, height);

        if (touchController != null) {
            touchController.setScreenSize(width, height);
        }
    }

    // 新增：处理触摸事件的方法（替代原来的handleTouch）
    public void handleTouch(int x, int y) {
        if (touchController != null) {
            // 这里我们直接调用触摸控制器的方法
            // 但由于onTouch需要View和MotionEvent参数，我们可以创建简化版本
            handleTouchPosition(x, y);
        }
    }

    // 简化版的触摸处理
    private void handleTouchPosition(int x, int y) {
        if (heroAircraft == null || gameOverFlag) {
            return;
        }

        // 限制英雄机在屏幕范围内
        int heroX = Math.max(0, Math.min(x, screenWidth));
        int heroY = Math.max(0, Math.min(y, screenHeight));

        // 更新英雄机位置
        heroAircraft.setLocation(heroX, heroY);
    }

    // ========== 绘制相关方法 ==========

    protected void drawBackground(Canvas canvas) {
        // 默认使用背景1，子类可以重写
        Bitmap background = ImageManager.BACKGROUND1_IMAGE;
        if (background != null) {
            // 简单平铺背景
            int bgHeight = background.getHeight();
            for (int y = 0; y < screenHeight; y += bgHeight) {
                canvas.drawBitmap(background, 0, y, paint);
            }
        } else {
            // 没有背景图片时使用纯色
            canvas.drawColor(Color.BLACK);
        }
    }

    protected void drawFlyingObjects(Canvas canvas, List<? extends AbstractFlyingObject> objects) {
        for (AbstractFlyingObject obj : objects) {
            if (obj.notValid()) continue;

            Bitmap image = obj.getImage();
            if (image != null) {
                int x = obj.getLocationX() - image.getWidth() / 2;
                int y = obj.getLocationY() - image.getHeight() / 2;
                canvas.drawBitmap(image, x, y, paint);
            }
        }
    }

    protected void drawHeroAircraft(Canvas canvas) {
        if (heroAircraft == null || heroAircraft.notValid()) return;

        Bitmap heroImage = heroAircraft.getImage();
        if (heroImage != null) {
            int x = heroAircraft.getLocationX() - heroImage.getWidth() / 2;
            int y = heroAircraft.getLocationY() - heroImage.getHeight() / 2;
            canvas.drawBitmap(heroImage, x, y, paint);
        }
    }

    protected void drawUI(Canvas canvas) {
        // 绘制分数
        paint.setColor(Color.RED);
        paint.setTextSize(40);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("SCORE: " + score, 20, 50, paint);

        // 绘制生命值
        if (heroAircraft != null) {
            canvas.drawText("LIFE: " + heroAircraft.getHp(), 20, 100, paint);
        }

        // 游戏结束时显示Game Over
        if (gameOverFlag) {
            paint.setTextSize(80);
            paint.setColor(Color.YELLOW);
            String gameOverText = "GAME OVER";
            float textWidth = paint.measureText(gameOverText);
            canvas.drawText(gameOverText, (screenWidth - textWidth) / 2, screenHeight / 2, paint);
        }
    }

    // ========== 其他补充方法 ==========

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public boolean isGameOver() {
        return gameOverFlag;
    }

    public interface GameOverListener {
        void onGameOver(int finalScore);
    }

    // 添加成员变量
    private GameOverListener gameOverListener;

    // 添加setter方法
    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    public void onGameOver() {
        int finalScore = getScore();

        // 通知监听器
        if (gameOverListener != null) {
            gameOverListener.onGameOver(finalScore);
        }
    }

    public int getScore() {
        return score;
    }

    public HeroAircraft getHeroAircraft() {
        return heroAircraft;
    }

    public void cleanup() {
        Log.d("GameController", "开始清理游戏控制器");

        // 停止执行器服务
        if (executorService != null && !executorService.isShutdown()) {
            try {
                executorService.shutdown();
                if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 清理列表
        if (enemyAircrafts != null) {
            enemyAircrafts.clear();
        }
        if (heroBullets != null) {
            heroBullets.clear();
        }
        if (enemyBullets != null) {
            enemyBullets.clear();
        }
        if (allProps != null) {
            allProps.clear();
        }

        // 重置游戏状态
        gameOverFlag = true;

        // 移除监听器
        gameOverListener = null;

        resetTime();

        // 清理触摸控制器
        if (touchController != null) {
            // 如果TouchController有cleanup方法，调用它
        }

        Log.d("GameController", "游戏控制器清理完成");
    }
    protected void resetTime() {
        this.time = 0;
        enemyShootScheduler.resetTimer();
        enemySpawnScheduler.resetTimer();
        heroShootScheduler.resetTimer();
    }



    // ========== 抽象方法（由子类实现，控制不同难度行为） ==========

    protected void crashChecks() {
        // ======================
        // 1. 敌机子弹攻击英雄
        // ======================
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) continue;
            if (heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
                if (soundEnabled) {
//                    getMusicManager().playBulletHit(); // 英雄被击中音效，还未开始音效相关工作
                }
            }
        }

        // ======================
        // 2. 英雄子弹攻击敌机，以及碰撞、道具掉落
        // ======================
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) continue;

            for (EnemyAircraft enemy : enemyAircrafts) {
                if (enemy.notValid()) continue;

                if (enemy.crash(bullet)) {
                    // 敌机被击中，扣血
                    enemy.decreaseHp(bullet.getPower());
                    bullet.vanish();

                    if (enemy.notValid()) {
                        // 敌机被击毁
                        score += enemy.getScore();
                        List<Integer> propTypeIndices = enemy.dropPropTypes();
                        if (propTypeIndices != null && !propTypeIndices.isEmpty()) {
                            int totalProps = propTypeIndices.size(); // 总共要掉几个道具
                            for (int i = 0; i < totalProps; i++) {
                                Integer propTypeIndex = propTypeIndices.get(i); // 第 i 个道具类型

                                // 横向扇形偏移计算（类似子弹发射的扇形分布）
                                int offsetIndex = i;
                                int offset_x = (offsetIndex * 2 - totalProps + 1) * 15; // 15 是横向间距，可调整！
                                int propX = enemy.getLocationX() + offset_x; // X 坐标偏移
                                int propY = enemy.getLocationY() + 10; // Y 坐标稍微向下，更自然

                                // 创建道具并添加到列表
                                AbstractProp prop = createPropByType(propTypeIndex, propX, propY);
                                if (prop != null) {
                                    allProps.add(prop);
                                }
                            }
                        }

                        // 如果是 Boss，切换背景音乐
                        if (enemy instanceof BossEnemy) {
                            if (soundEnabled) {
//                                getMusicManager().playBackgroundMusic(); // 切回普通背景音乐
//                                // System.out.println("Boss 被击败，切换回普通背景音乐");
                            }
                            enemySpawnManager.onBossDestroyed(); // Boss 被击败，允许下次生成
                        }
                    }
                }

                // 英雄与敌机相撞（互相毁灭）
                if (enemy.crash(heroAircraft) || heroAircraft.crash(enemy)) {
                    enemy.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE); // 致命伤害

                }
            }
        }

        // ======================
        // 3. 英雄拾取道具
        // ======================
        for (AbstractProp prop : allProps) {
            if (prop.notValid()) continue;
            if (heroAircraft.crash(prop)) {
                // ---- 先判断是什么道具类型 ----
                if (soundEnabled) {
                    if (prop instanceof PropBomb) {
                        // 如果是炸弹道具
//                        getMusicManager().playBombExplosion(); // 播放炸弹爆炸音效
                    } else if (prop instanceof PropBlood) {

//                        getMusicManager().playGetSupply();

                    } else {
                        // 如果是火力道具
//                        getMusicManager().playBulletShoot(); // 播放射击/火力音效
                    }
                }

                // ---- 然后应用效果并移除道具 ----
                prop.applyEffect(heroAircraft);
                prop.vanish();
            }
        }
    }

    /** 敌机最大数量，子类可覆写 */
    protected int getEnemyMaxNumber() {return 7;}

    /** 是否允许生成敌机，子类可控制（如随时间关闭生成） */
    protected boolean shouldGenerateEnemies() {return true;}

    /**
     * 生成敌机（通用流程，由父类控制）：
     * 1. 检查是否允许生成敌机（比如某些难度后期停止生成）
     * 2. 检查敌机数量是否已达上限
     * 3. 调用子类实现的 createEnemyWithSpawnManager()，让子类决定如何生成敌机
     * 4. 若返回的敌机不为空，则添加到列表并注册炸弹观察者
     */
    protected void generateEnemies() {
        // 1. 是否允许生成敌机（子类可控制，比如后期不生成）
        if (!shouldGenerateEnemies()) {
            return;
        }

        // 2. 是否已达到敌机数量上限
        if (enemyAircrafts.size() >= getEnemyMaxNumber()) {
            return;
        }

        // 3. 调用子类方法：由子类使用 enemySpawnManager（或其它方式）生成敌机
        EnemyAircraft enemy = createEnemyWithSpawnManager();

        // 4. 若敌机有效，则添加并注册
        if (enemy != null) {
            enemyAircrafts.add(enemy);
            registerEnemyWithBombs(enemy);
        }
    }

    /**
     * 子类必须实现此方法，用于实际生成一个敌机（通常通过 enemySpawnManager）
     * @return 生成的敌机对象，可能为 null（表示本次不生成）
     */
    protected EnemyAircraft createEnemyWithSpawnManager() {
        return enemySpawnManager.generateEnemy(screenWidth, screenHeight, score);
    }

    /** 时间相关逻辑：如难度递增、Boss生成、特效触发等，子类实现 */
    protected void onTimeIncrease() {}

    protected void enemyShootAction() {
        // 敌机射击
        for (EnemyAircraft enemy : enemyAircrafts) {
            List<BaseBullet> bullets = enemy.shoot();
            if (bullets != null) {
                enemyBullets.addAll(bullets);
                for (BaseBullet bullet : bullets) {
                    if (bullet instanceof EnemyBullet) {
                        registerBulletWithBombs((EnemyBullet) bullet);
                    }
                }
            }
        }
    }

    protected void heroShootAction() {
        // 英雄射击
        heroBullets.addAll(heroAircraft.shoot());
    }
    /** 移动逻辑：子弹、敌机、道具 */
    protected void moveActions() {
        for (BaseBullet bullet : heroBullets) bullet.forward();
        for (BaseBullet bullet : enemyBullets) bullet.forward();
        for (EnemyAircraft enemy : enemyAircrafts) enemy.forward();
        for (AbstractProp prop : allProps) prop.forward();
    }

    /** 清理无效对象：子弹、敌机、道具 */
    protected void postProcesses() {

        // 先处理被炸弹清除的敌机分数
        processDestroyedEnemies();

//        enemyBullets.removeIf(AbstractFlyingObject::notValid);
//        heroBullets.removeIf(AbstractFlyingObject::notValid);
//        使用对象池回收子弹
        // 回收并清理英雄子弹
        java.util.Iterator<BaseBullet> heroIter = heroBullets.iterator();
        while (heroIter.hasNext()) {
            BaseBullet bullet = heroIter.next();
            if (bullet.notValid()) {
                // 核心逻辑：在移除前放回对象池
                if (bullet instanceof HeroBullet) {
                    BulletPool.recycleH((HeroBullet) bullet);
                }
                heroIter.remove();
            }
        }

        // 回收并清理敌机子弹
        java.util.Iterator<BaseBullet> enemyIter = enemyBullets.iterator();
        while (enemyIter.hasNext()) {
            BaseBullet bullet = enemyIter.next();
            if (bullet.notValid()) {
                // 核心逻辑：在移除前放回对象池
                if (bullet instanceof EnemyBullet) {
                    BulletPool.recycleE((EnemyBullet) bullet);
                }
                enemyIter.remove();
            }
        }

        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        allProps.removeIf(AbstractFlyingObject::notValid);
    }

    /** 检查游戏是否结束（默认：英雄死亡） */
    protected void checkGameOver() {
        if (heroAircraft.getHp() <= 0 && !gameOverFlag) {
            if (soundEnabled) {
                // 音效相关代码
            }
            // System.out.println("英雄机死亡，游戏结束！");

            gameOverFlag = true;

            // 确保在主线程调用onGameOver
            if (gameOverListener != null) {
                // 获取主线程Handler
                Activity activity = (Activity) context;
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        gameOverListener.onGameOver(getScore());
                    });
                } else {
                    // 如果无法获取Activity，尝试其他方式
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        if (gameOverListener != null) {
                            gameOverListener.onGameOver(getScore());
                        }
                    });
                }
            }

            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
        }
    }

    /** 注册敌机到炸弹观察者 */
    protected void registerEnemyWithBombs(EnemyAircraft enemy) {
        for (AbstractProp prop : allProps) {
            if (prop instanceof PropBomb) {
                ((PropBomb) prop).registerObserver((Observer) enemy);
            }
        }
    }

    /** 注册敌机子弹到炸弹观察者 */
    protected void registerBulletWithBombs(EnemyBullet bullet) {
        for (AbstractProp prop : allProps) {
            if (prop instanceof PropBomb) {
                ((PropBomb) prop).registerObserver((Observer) bullet);
            }
        }
    }

    /** 处理被炸弹清除的敌机，增加分数 */
    protected void processDestroyedEnemies() {
        List<EnemyAircraft> destroyed = new ArrayList<>();
        for (EnemyAircraft e : enemyAircrafts) {
            if (e.notValid() && e.isDestroyedByBomb()) {
                destroyed.add(e);
            }
        }
        for (EnemyAircraft e : destroyed) {
            score += e.getScore();
            // System.out.println("炸弹击毁敌机，得分：" + e.getScore());
        }
        for (EnemyAircraft e : enemyAircrafts) {
            e.setDestroyedByBomb(false);
        }
    }

    /** 工具：根据类型创建道具 */
    protected AbstractProp createPropByType(int type, int x, int y) {
        switch (type) {
            case 0: return new PropBloodFactory().createProp(x, y, 0, 10);
            case 1: return new PropBulletFactory().createProp(x, y, 0, 10);
            case 2:
                AbstractProp bomb = new PropBombFactory().createProp(x, y, 0, 10);
                if (bomb instanceof PropBomb) ((PropBomb) bomb).setGame(this);
                return bomb;
            case 3: return new PropBulletPlusFactory().createProp(x, y, 0, 10);
            default: return null;
        }
    }

    /** 工具：调度 Buff 恢复 */
    public void scheduleBuffRestore(HeroAircraft hero, ShootStrategyManager manager) {
        executorService.schedule(() -> {
            if (hero != null && manager != null) {
                hero.setShootNum(1);
                manager.applyStraightEffect();
            }
        }, 10, TimeUnit.SECONDS);
    }


    /**
     * 初始化各个行为调度器，设置初始的触发频率
     * 可由子类覆写以针对不同难度设置不同的 interval
     */
    protected void initSchedulers() {
        // 敌机生成：每X秒检查是否生成敌机（调用 tryGenerateEnemies()）
        enemySpawnScheduler = new BehaviorScheduler(this::tryGenerateEnemies, 600f);

        // 敌机射击：每X秒执行一次 enemyShootAction()
        enemyShootScheduler = new BehaviorScheduler(this::enemyShootAction, 600f);

        // 英雄射击：每X秒执行一次 heroShootAction()
        heroShootScheduler = new BehaviorScheduler(this::heroShootAction, 500f);
    }

    // ========== Getters（可选） ==========

//    protected boolean isSoundEnabled() { return musicManager != null; }
//    protected MusicManager getMusicManager() { return musicManager; }

    // 提供给 PropBomb 直接访问当前敌机和子弹
    public List<EnemyAircraft> getEnemyAircrafts() {
        return enemyAircrafts;
    }

    public List<BaseBullet> getEnemyBullets() {
        return enemyBullets;
    }

    // 用于增加分数（可由炸弹调用）
    public void addScore(int points) {
        this.score += points;
        // System.out.println("分数增加：" + points);
    }

    /**
     * 封装敌机生成逻辑，由调度器调用
     * 代替原来的 if (shouldGenerateEnemies()) generateEnemies();
     */
    protected void tryGenerateEnemies() {
        if (shouldGenerateEnemies()) {
            generateEnemies();
        }
    }

}
