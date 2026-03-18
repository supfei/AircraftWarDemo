package com.example.aircraftwardemo.observer;

import com.example.aircraftwardemo.observer.Observer;

/**
 * 被观察者接口（Subject）：定义注册、移除、通知观察者的方法
 */
public interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers();
}
