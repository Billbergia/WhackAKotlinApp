package com.example.myfirstkotlinapp;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class MainThread extends Thread {

    private SurfaceHolder surfaceHolder;
    private GameView gameView;

    private boolean isRunning;
    private int targetFps = 30;
    private double averageFps = 0;

    public static Canvas canvas;

    public MainThread(SurfaceHolder surfaceHolder, GameView gameView) {
        super();

        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
    }

    @Override
    public void run() {
        long startTime, timeInMilliseconds, waitTime;
        long totalTime = 0;
        long targetTime = 1000 / targetFps;
        int frameCount = 0;

        while (isRunning) {
            startTime = System.nanoTime();
            canvas = null;

            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    this.gameView.update();
                    this.gameView.draw(canvas);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                if(canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }

            timeInMilliseconds = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - timeInMilliseconds;

            try {
                this.sleep(waitTime);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            totalTime += System.nanoTime() - startTime;
            frameCount++;
            if(frameCount == targetFps) {
                averageFps = 1000 / ((totalTime / frameCount) / 1000000);
                frameCount = 0;
                totalTime = 0;
                System.out.println(averageFps);
            }
        }
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = true;
    }
}
