package com.example.myfirstkotlinapp.gamecore;

/** Singleton instance for common runtime values for game logic such as delta time etc. */
public class GameGlobal {
    protected float _deltaTime = 0;
    protected float _currentTime = 0;
    protected long _averageFps = 0;

    private static GameGlobal single_instance = null;

    protected static GameGlobal getInstance() {
        if (single_instance == null)
            single_instance = new GameGlobal();

        return single_instance;
    }

    /** Retrieves delta time, this is the time difference since the last frame in seconds. */
    public static float deltaTime() {
        return GameGlobal.getInstance()._deltaTime;
    }

    /** Retrieves current time, this is the time in seconds. */
    public static float currentTime() { return GameGlobal.getInstance()._currentTime; }

    public static long averageFps() {
        return GameGlobal.getInstance()._averageFps;
    }
}
