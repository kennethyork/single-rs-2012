package com.rs.android;

/**
 * Static bridge between the Android host and the AWT/game engine thread.
 */
public final class GameBridge {
    public static volatile GameSurfaceView surfaceView;
    public static volatile boolean running;

    private GameBridge() {}
}
