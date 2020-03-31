package com.lint0t.linmusicplayer;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class MyThreadPool {
    private int maxThread = 1, taskNumber = 1, mode = 0, delay = 0,period = 0;
    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool(), fixedThreadPool = Executors.newFixedThreadPool(maxThread), singleThreadPool;
    private ScheduledExecutorService scheduledExecutorService;
    public static final int USE_CACHEDTHREADPOOL = 0, USE_FIXEDTHREADPOOL = 1, USE_SINGLETHREADPOOL = 2, USE_SCHEDULEEXECUTORSERVICE = 3;

    private Runnable runnable;
    private Runnable[] runnables;
    private TimeUnit timeUnit = TimeUnit.SECONDS;


    public MyThreadPool() {
    }

    public MyThreadPool setMaxThread(int maxThread) {
        this.maxThread = maxThread;
        return this;
    }

    public MyThreadPool setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    public MyThreadPool setTaskNumber(int i) {
        this.taskNumber = i;
        return this;
    }

    public MyThreadPool setDelay(int i) {
        this.delay = i;
        return this;
    }

    public MyThreadPool setPeriod(int i) {
        this.period = i;
        return this;
    }

    public MyThreadPool setTimeUnit(TimeUnit t) {
        this.timeUnit = t;
        return this;
    }

    public MyThreadPool setRunnables(int i, Runnable runnable) {
        this.runnables[i] = runnable;
        return this;
    }

    public MyThreadPool setMode(int i) {
        this.mode = i;
        return this;
    }

    public MyThreadPool go() {
        if (runnable == null) {
            Log.d(TAG, "runnable is null");
        } else {
            switch (mode) {
                case USE_CACHEDTHREADPOOL:
                    cached();
                    break;
                case USE_FIXEDTHREADPOOL:
                    fixed();
                    break;
                case USE_SINGLETHREADPOOL:
                    single();
                    break;
                case USE_SCHEDULEEXECUTORSERVICE:
                    scheduled();
                    break;
            }
        }
        return this;
    }

    private void cached() {
        for (int i = 0; i < taskNumber; i++) {
            cachedThreadPool.execute(runnable);

        }
    }

    private void fixed() {
        for (int i = 0; i < taskNumber; i++) {
            fixedThreadPool.execute(runnable);
        }
    }

    private void single() {
        singleThreadPool = Executors.newSingleThreadExecutor();
        for (int i = 0; i < taskNumber; i++) {
            singleThreadPool.execute(runnable);
        }
    }

    private void scheduled() {
        if (period == 0) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.schedule(runnable, delay, timeUnit);
        }
        else {
            scheduledExecutorService.scheduleAtFixedRate(runnable,delay,period,timeUnit);
        }
    }

    private void shutDown(){
        if (period != 0)
        scheduledExecutorService.shutdown();
    }

}
