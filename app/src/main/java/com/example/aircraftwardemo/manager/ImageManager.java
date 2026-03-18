package com.example.aircraftwardemo.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.aircraftwardemo.model.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 综合管理图片的加载，访问
 * 提供图片的静态访问方法
 *
 * @author hitsz
 */
public class ImageManager {
    private static Context appContext;
//    游戏初始化时传入appContext
    public static void init(Context context) {
        appContext = context.getApplicationContext();
        loadImages();
    }

    /**
     * 类名-图片 映射，存储各基类的图片 <br>
     * 可使用 CLASSNAME_IMAGE_MAP.get( obj.getClass().getName() ) 获得 obj 所属基类对应的图片
     */
    private static final Map<String, Bitmap> CLASSNAME_IMAGE_MAP = new HashMap<>();

    public static Bitmap BACKGROUND1_IMAGE;
    public static Bitmap BACKGROUND2_IMAGE;
    public static Bitmap BACKGROUND3_IMAGE;
    public static Bitmap BACKGROUND4_IMAGE;
    public static Bitmap BACKGROUND5_IMAGE;
    public static Bitmap BACKGROUND_START_IMAGE;
    public static Bitmap HERO_IMAGE;
    public static Bitmap HERO_BULLET_IMAGE;
    public static Bitmap ENEMY_BULLET_IMAGE;
    public static Bitmap MOB_ENEMY_IMAGE;
    public static Bitmap ELITE_ENEMY_IMAGE;
    public static Bitmap ELITE_PLUS_ENEMY_IMAGE;
    public static Bitmap BOSS_ENEMY_IMAGE;

    public static Bitmap PROP_BLOOD_IMAGE;
    public static Bitmap PROP_BOMB_IMAGE;
    public static Bitmap PROP_BULLET_IMAGE;
    public static Bitmap PROP_BULLET_Plus_IMAGE;

    private static void loadImages() {
        try {
            // 使用Android方式加载图片
            BACKGROUND1_IMAGE = loadBitmap("bg1");  // 对应原来的bg.jpg
            BACKGROUND2_IMAGE = loadBitmap("bg2");
            BACKGROUND3_IMAGE = loadBitmap("bg3");
            BACKGROUND4_IMAGE = loadBitmap("bg4");
            BACKGROUND5_IMAGE = loadBitmap("bg5");
            BACKGROUND_START_IMAGE = loadBitmap("bg_start");

            HERO_IMAGE = loadBitmap("hero");
            MOB_ENEMY_IMAGE = loadBitmap("mob");
            ELITE_ENEMY_IMAGE = loadBitmap("elite");
            BOSS_ENEMY_IMAGE = loadBitmap("boss");
            ELITE_PLUS_ENEMY_IMAGE = loadBitmap("elite_plus");
            HERO_BULLET_IMAGE = loadBitmap("bullet_hero");
            ENEMY_BULLET_IMAGE = loadBitmap("bullet_enemy");

            PROP_BLOOD_IMAGE = loadBitmap("prop_blood");
            PROP_BOMB_IMAGE = loadBitmap("prop_bomb");
            PROP_BULLET_IMAGE = loadBitmap("prop_bullet");
            PROP_BULLET_Plus_IMAGE = loadBitmap("prop_bullet_plus");

            // 初始化映射表（保持原有映射逻辑）
            CLASSNAME_IMAGE_MAP.put(HeroAircraft.class.getName(), HERO_IMAGE);
            CLASSNAME_IMAGE_MAP.put(MobEnemy.class.getName(), MOB_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(EliteEnemy.class.getName(), ELITE_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(BossEnemy.class.getName(), BOSS_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(ElitePlusEnemy.class.getName(), ELITE_PLUS_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(HeroBullet.class.getName(), HERO_BULLET_IMAGE);
            CLASSNAME_IMAGE_MAP.put(EnemyBullet.class.getName(), ENEMY_BULLET_IMAGE);
            CLASSNAME_IMAGE_MAP.put(PropBlood.class.getName(), PROP_BLOOD_IMAGE);
            CLASSNAME_IMAGE_MAP.put(PropBomb.class.getName(), PROP_BOMB_IMAGE);
            CLASSNAME_IMAGE_MAP.put(PropBullet.class.getName(), PROP_BULLET_IMAGE);
            CLASSNAME_IMAGE_MAP.put(PropBulletPlus.class.getName(), PROP_BULLET_Plus_IMAGE);

        } catch (Exception e) {
            e.printStackTrace();
            // Android中不要调用System.exit()
            throw new RuntimeException("Failed to load images", e);
        }
    }

    /**
     * 从资源加载Bitmap
     */
    private static Bitmap loadBitmap(String imageName) {
        int resId = getResourceId(imageName);
        if (resId == 0) {
            throw new RuntimeException("Image resource not found: " + imageName);
        }
        return BitmapFactory.decodeResource(appContext.getResources(), resId);
    }

    /**
     * 根据图片名称获取资源ID
     */
    private static int getResourceId(String imageName) {
        // 资源名称到drawable资源ID的映射
        return appContext.getResources().getIdentifier(
                imageName,          // 资源名称（与drawable中的文件名一致）
                "drawable",         // 资源类型
                appContext.getPackageName()  // 包名
        );
    }

    /**
     * 保持原有的get方法API
     */
    public static Bitmap get(String className) {
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static Bitmap get(Object obj) {
        if (obj == null) {
            return null;
        }
        return get(obj.getClass().getName());
    }

    /**
     * 资源释放方法（Android需要手动管理Bitmap内存）
     */
    public static void recycleAll() {
        recycleBitmap(BACKGROUND1_IMAGE);
        recycleBitmap(BACKGROUND2_IMAGE);
        recycleBitmap(BACKGROUND3_IMAGE);
        recycleBitmap(BACKGROUND4_IMAGE);
        recycleBitmap(BACKGROUND5_IMAGE);
        recycleBitmap(BACKGROUND_START_IMAGE);
        recycleBitmap(HERO_IMAGE);
        recycleBitmap(HERO_BULLET_IMAGE);
        recycleBitmap(ENEMY_BULLET_IMAGE);
        recycleBitmap(MOB_ENEMY_IMAGE);
        recycleBitmap(ELITE_ENEMY_IMAGE);
        recycleBitmap(ELITE_PLUS_ENEMY_IMAGE);
        recycleBitmap(BOSS_ENEMY_IMAGE);
        recycleBitmap(PROP_BLOOD_IMAGE);
        recycleBitmap(PROP_BOMB_IMAGE);
        recycleBitmap(PROP_BULLET_IMAGE);
        recycleBitmap(PROP_BULLET_Plus_IMAGE);

        for (Bitmap bitmap : CLASSNAME_IMAGE_MAP.values()) {
            recycleBitmap(bitmap);
        }
        CLASSNAME_IMAGE_MAP.clear();
    }

    private static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

}
