package com.example.db.util;

/**
 * 计算程序耗时工具
 */
public class CostUtil {
    /** 起始时间 */
    private long startTime;
    /** 暂停时间 */
    private long pauseTime;
    /** 中途休息了多久 */
    private long sleepTime;

    public CostUtil() {
        begin();
    }

    /**
     * 当前时间
     */
    private long current() {
        return System.currentTimeMillis();
    }

    /**
     * 程序开始计时
     */
    public void begin() {
        startTime = current();
        pauseTime = 0;
        sleepTime = 0;
    }

    /**
     * 暂停
     */
    public void pause() {
        if(pauseTime == 0) pauseTime = current();
    }

    /**
     * 恢复
     */
    public void resume() {
        if(pauseTime != 0) {
            sleepTime += (current() - pauseTime);
            pauseTime = 0;
        }
    }

    /**
     * 返回程序执行时长
     */
    public long end() {
        long stopTime = pauseTime == 0 ? current() : pauseTime;
        return stopTime - startTime - sleepTime;
    }

    /**
     * 计算程序耗时
     */
    public static long costTime(Runnable runnable) {
        CostUtil costUtil = new CostUtil();
        runnable.run();
        return costUtil.end();
    }
}
