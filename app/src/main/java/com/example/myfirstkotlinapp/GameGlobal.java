package com.example.myfirstkotlinapp;

public class GameGlobal {
    private static GameGlobal single_instance = null;

    // Time since last frame.
    public float _deltaTime = 0;
    public long _averageFps = 0;

    public static GameGlobal getInstance() {
        if (single_instance == null)
            single_instance = new GameGlobal();

        return single_instance;
    }

    public static float deltaTime() {
        return GameGlobal.getInstance()._deltaTime;
    }

    public static long averageFps() {
        return GameGlobal.getInstance()._averageFps;
    }
}
