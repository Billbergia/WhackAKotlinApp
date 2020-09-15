package com.example.myfirstkotlinapp;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {

    private final SurfaceHolder surfaceHolder;
    private GameView gameView;
    private GameGlobal gameGlobal;

    private boolean running;
    private int targetFps = 60;
    private int targetTimeMs = 1000 / targetFps;

    public static Canvas canvas;

    public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
        super();

        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
        this.gameGlobal = GameGlobal.getInstance();
    }

    @Override
    public void run() {
        long currentTimeNs = System.nanoTime();
        long lastUpdateTimeNs = currentTimeNs;
        long deltaTimeNs = 0;
        long waitTimeNs = 0;

        long totalTime = 0;
        int frameCount = 0;

        while (this.running) {
            currentTimeNs = System.nanoTime();
            deltaTimeNs = currentTimeNs - lastUpdateTimeNs;

            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    // Give update deltaTime in seconds instead of nano seconds.
                    this.gameGlobal._deltaTime = (float)(deltaTimeNs / 1_000_000_000.0);
                    this.gameView.update((float)(deltaTimeNs / 1_000_000_000.0));
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

            // Try to stay close to the targetTime per frame.
            // If frame was too fast, sleep remaining time.
            waitTimeNs = targetTimeMs - (System.nanoTime() - currentTimeNs) / 1_000_000;
            if (waitTimeNs > 0) {
                try {
                    this.sleep(waitTimeNs);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            // Calculate average fps. Just for debug purposes.
            totalTime += System.nanoTime() - currentTimeNs;
            frameCount++;
            if(frameCount == targetFps) {
                GameGlobal.getInstance()._averageFps = 1000 / ((totalTime / frameCount) / 1_000_000);
                frameCount = 0;
                totalTime = 0;
            }

            lastUpdateTimeNs = currentTimeNs;
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
