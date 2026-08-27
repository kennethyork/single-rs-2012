package com.rs.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

/**
 * Main Android Activity. Hosts the game on a background thread and renders the
 * client's software frames to the GameSurfaceView.
 */
public class GameActivity extends Activity {

    private GameSurfaceView surfaceView;
    private Thread gameThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUI();

        Log.i(AndroidLoader.TAG, "activity: onCreate");
        AndroidPlatform.init(this);

        surfaceView = new GameSurfaceView(this);
        setContentView(surfaceView);
        GameBridge.surfaceView = surfaceView;
        GameBridge.running = true;

        gameThread = new Thread(() -> {
            try {
                // Instantiating Loader is also the first load of the java.awt
                // shim (Loader extends java.awt.Panel), so it is logged
                // separately -- whether ART accepts classes in a java.* package
                // is the single biggest runtime unknown in this port.
                com.rs.Loader loader = new com.rs.Loader();
                Log.i(AndroidLoader.TAG, "activity: java.awt shim loaded, Loader instantiated");
                AndroidLoader.boot(loader);
            } catch (Throwable t) {
                Log.e(AndroidLoader.TAG, "activity: game thread died", t);
            } finally {
                GameBridge.running = false;
                Log.i(AndroidLoader.TAG, "activity: game thread exited");
            }
        }, "RS2012-GameThread");
        gameThread.setPriority(Thread.NORM_PRIORITY);
        gameThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GameBridge.running = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUI();
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
