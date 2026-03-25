## 0318：触摸功能和最基础功能实现。
## 0320：实现了基本的结束界面和本地排行榜。
##### 今日解决了崩溃问题：
观察logcat发现在游戏启动的时候gameview同时启动了两个gamethread，导致了流速翻倍，两个线程都在执行updateGame（），而我一直在怀疑是初始化time的问题。也是同样的原因导致实机测试闪退崩溃。
##### 原因是：
在 Android 生命周期中，`MainActivity.onResume()` 触发了 `resumeGame()` 启动了一个线程，紧接着 `SurfaceView` 的 `surfaceCreated()` 又被回调，不加判断地又启动了一个线程。
相关代码：
```
@Override  
protected void onResume() {  
    super.onResume();  
    Log.d("MainActivity", "Activity恢复");  
  
    if (gameView != null) {  
        gameView.resumeGame();  
    }  
}
```

```
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
```

```
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
```

##### 疑惑：什么情况下会触发activity的onResume：
在Android中，`onResume()`和`onPause()`会在以下情况下触发：

- **来电/去电**：电话打入或打出
    
- **通知栏**：下拉或收起通知栏
    
- **系统弹窗**：如电量低、权限请求等
    
- **分屏模式**：进入或退出分屏
    
- **画中画模式**：视频应用常用
    
- **最近任务视图**：打开最近应用列表
    
- **锁屏/解锁**：按下电源键锁屏，然后解锁
    
- **其他应用覆盖**：如分享菜单、文件选择器等

##### 接下来的改进方向：
- 1.区分暂停和停止，现在只有游戏终止处理，并没有游戏暂停处理。
- 2.更解耦：创建游戏模式管理工厂；有多余的精力考虑把draw逻辑从GameController移出。
- 3.生命周期：游戏线程应该与Surface绑定，而不是与Activity绑定。
    - Activity生命周期: onCreate → onStart → onResume → onPause → onStop → onDestroy
    - Surface生命周期: surfaceCreated → surfaceChanged → surfaceDestroyed
 ## 0322：使用工厂模式管理不同难度的游戏生成
 ##### 小优化：注释所有了sout，给属性增加添加了上限，缓解渲染掉帧
 ##### 待解决大问题：游戏在中后期，每一秒钟都在创建和销毁成百上千个对象。系统内存扛不住这种消耗，触发了频繁的垃圾回收（GC）。GC 运行时会暂停所有线程（包括你的游戏渲染线程），这会造成肉眼可见的严重卡顿（内存抖动）。
 ##### 暂时的想法：使用对象池
 ## 0324：创建并使用子弹对象池
 #### 1.实现了对象池类，搞定了子弹对象池
解决了渲染掉帧以及gc（垃圾回收）问题。
只实现子弹对象池是因为之前给敌机生成加上限了，最主要原因是子弹数量太多频繁增删导致内存抖动。创建敌机对象池等可作为有余力时的优化空间。
##### 问题回顾
- 内存抖动：在短时间内频繁增删大量临时对象，导致内存占用波动。
- GC（garbage collection）：内存不足时GC会启动，**扫描**内存中的对象引用树。为了确保扫描时对象不被移动，它会**挂起（暂停）** 所有正在运行的线程（包括渲染线程和逻辑线程）。
- ANR（application not responding)：频繁GC导致系统弹出ANR应用程序无响应。
##### 对象池
- 原理：空间换时间，预先申请内存放对象，使用对象时从池子里借，用完属性重置归还。
- 起作用的原因：不需要老new，内存占用稳定，不会频繁GC。
#### 2.待实现
- 貌似简单模式不能有boss
- 给道具前进轨迹加散射和左右边界反弹
- 音乐播放：还没讲
- 排行榜的删改查（也许实现）
- 服务器部署：还没讲
- 对战功能（想法）：
	- 登录账号/输入用户名（防重复）
	- 输入房间号
	- 在游戏界面上实时获取对方的分数并显示
- 优化ui（也许）：全屏会挡住实机的状态栏，有点丑
## 0325：改了多道具前进轨迹
#### 新发现问题：PropBulletPlus里new了线程会导致线程竞争
