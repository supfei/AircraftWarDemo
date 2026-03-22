package com.example.aircraftwardemo.model;

/**
 * @author 有朝宿云
 * Created on 2025/09/21 13:41
 **/
public class PropBlood extends AbstractProp {
    public PropBlood(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void applyEffect(HeroAircraft hero) {
        hero.increaseHp(20);
        // System.out.println("Hp++");
    }
}