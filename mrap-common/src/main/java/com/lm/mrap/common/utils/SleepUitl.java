package com.lm.mrap.common.utils;

import java.util.concurrent.TimeUnit;

/**
 * @author liming
 * @version 1.0
 * @description: sleep工具类
 * @date 2022/10/20 下午1:53
 */
public class SleepUitl {
    public static void threadSleep(int sleepTime) throws InterruptedException {
        Thread.sleep(sleepTime);
    }

    /**
     * e.g 100-1000 (控制线程睡眠在 100ms 到 1000ms)
     *
     * @param timeStr 睡眠时间
     * @throws InterruptedException InterruptedException
     */
    public static void threadSleep(String timeStr) throws InterruptedException {
        String[] split =  timeStr.split("-");
        int min = Integer.parseInt(split[0]);
        int max = Integer.parseInt(split[1]);

        Thread.sleep((min + (int) (Math.random() * (max - min))));
    }

    public static void milliSeconds(long seconds) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(seconds);
    }

    public static void second(long seconds) throws InterruptedException {
        TimeUnit.SECONDS.sleep(seconds);
    }

    public static void minute(long minutes) throws InterruptedException {
        TimeUnit.MINUTES.sleep(minutes);
    }
}
