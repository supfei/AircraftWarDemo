package com.example.aircraftwardemo.pool;

import java.util.LinkedList;

public class ObjectPool<T> {
    private final LinkedList<T> pool = new LinkedList<>();

    // 从池中获取对象
    public T acquire() {
        if (pool.isEmpty()) {
            return null; // 池子空了，通知外部去 new
        }
        return pool.removeFirst();
    }

    // 将对象还回池子
    public void release(T obj) {
        if (obj != null && !pool.contains(obj)) {
            pool.addLast(obj);
        }
    }
}