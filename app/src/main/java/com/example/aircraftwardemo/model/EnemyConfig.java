package com.example.aircraftwardemo.model;

/**
 * 敌机配置类：用于集中管理某类敌机的属性，包括速度、血量、分数、道具掉落数量等
 * 可用于敌机工厂创建敌机时传入配置，实现灵活定制
 */
public class EnemyConfig {
    private int speedX;
    private int speedY;
    private int hp;
    private int score;
    private int propDropCount;

    public EnemyConfig(int speedX, int speedY, int hp, int score, int propDropCount) {
        this.speedX = speedX;
        this.speedY = speedY;
        this.hp = hp;
        this.score = score;
        this.propDropCount = propDropCount;
    }

    // Getter
    public int getSpeedX() { return speedX; }
    public int getSpeedY() { return speedY; }
    public int getHp() { return hp; }
    public int getScore() { return score; }
    public int getPropDropCount() { return propDropCount; }

    // ✅ Setter：允许动态修改属性
    public void setSpeedX(int speedX) { this.speedX = speedX; }
    public void setSpeedY(int speedY) { this.speedY = speedY; }
    public void setHp(int hp) { this.hp = hp; }
    public void setScore(int score) { this.score = score; }
    public void setPropDropCount(int propDropCount) { this.propDropCount = propDropCount; }
}