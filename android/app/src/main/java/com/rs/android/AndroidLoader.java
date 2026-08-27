package com.rs.android;

import android.util.Log;

import com.rs.Launcher;
import com.rs.Loader;
import com.rs.Settings;
import com.rs.jagex.client;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Entry point for the 2012 client on Android.
 *
 * Mirrors the desktop Loader's boot sequence (loadParams -> new client ->
 * supplyApplet -> init -> start) but hosts rendering through the Android
 * GameSurfaceView instead of an AWT Frame. The safe-mode JavaRenderer produces
 * the int[] ARGB frame that GameSurfaceView renders to the screen.
 */
public final class AndroidLoader {

    /**
     * Size the client renders at.
     *
     * Every frame the client reads Class371.getActiveContainer().getSize() and
     * resizes to it. On desktop that container is the Loader panel, which the
     * JFrame lays out to the size set in Loader.openFrame; on Android nothing
     * lays anything out, so the shim reported 0x0 and the client rendered 1x1
     * frames onto a black screen.
     *
     * Fixed rather than the surface's own size: this is a software renderer, and
     * a modern phone's full resolution would be far more pixels than it can push.
     * GameSurfaceView scales the result up to fit.
     */
    private static final int GAME_WIDTH = 774;
    private static final int GAME_HEIGHT = 588;

    /**
     * Boot checkpoints are logged under one tag. There is no debugger on a CI
     * emulator, so logcat is the only way to see how far the port got; the
     * emulator test in .github/workflows asserts on these lines.
     */
    public static final String TAG = "SingleRS";

    private AndroidLoader() {}

    /** Boots the client engine against the supplied headless Loader host. */
    public static void boot(Loader loader) {
        // Android has no OpenGL/DirectX native libraries: force the safe-mode
        // JavaRenderer (toolkit 0). Checked by GraphicsToolkitPreference.
        Log.i(TAG, "boot: starting, android=true");
        System.setProperty("darkan.android", "true");
        // Extract the game cache from assets into writable storage (the client
        // and the server's rs.darkan:core both open it with RandomAccessFile,
        // so it must be on disk). On the first launch this copies ~840 MB and
        // takes a minute or two, so report progress rather than sitting on a
        // black screen.
        try {
            AndroidPlatform.extractCacheIfNeeded(AndroidLoader::reportCacheProgress);
        } catch (java.io.IOException e) {
            Log.e(TAG, "boot: cache extraction failed", e);
            showStatus("Could not unpack the game cache: " + e.getMessage());
            return;
        }
        Log.i(TAG, "boot: cache ready at " + AndroidPlatform.getCacheDir());
        System.setProperty("darkan.cache.path", AndroidPlatform.getCacheDir());

        // The server reads its data through relative paths, which resolve
        // against "/" on Android. Unpack data/ and point darkan.data.path at it.
        showStatus("Unpacking game data\u2026");
        try {
            AndroidPlatform.extractDataIfNeeded();
        } catch (IOException e) {
            Log.e(TAG, "boot: data extraction failed", e);
            showStatus("Could not unpack the game data: " + e.getMessage());
            return;
        }
        Log.i(TAG, "boot: data ready at " + AndroidPlatform.getDataDir());
        System.setProperty("darkan.data.path", AndroidPlatform.getDataDir());

        // Single-player is what makes the server viable here: it is the flag
        // that skips MongoDB and the Undertow web API, neither of which works on
        // Android. worldConfig.json ships with singlePlayer false, but
        // Settings.isSinglePlayer() ORs in this property.
        System.setProperty("darkan.singlePlayer", "true");
        System.setProperty("darkan.save.path", AndroidPlatform.getSaveDir());
        new File(AndroidPlatform.getSaveDir()).mkdirs();
        // The client derives its own scratch paths from user.home, which is "/".
        System.setProperty("user.home", AndroidPlatform.getWritableDir());
        // It opens <user.home>/.darkanrs/caches/dk_cl_*.dat directly, without
        // creating the directory first.
        new File(AndroidPlatform.getWritableDir(), Loader.CACHE_DIR).mkdirs();

        if (!startWorldServer()) return;

        // loadParams() reads IP_ADDRESS into clientParams, and Properties
        // rejects a null value, so the address has to be set first -- the order
        // the desktop Loader.main uses.
        if (Loader.IP_ADDRESS == null) Loader.IP_ADDRESS = "127.0.0.1";
        Loader.loadParams();
        Log.i(TAG, "boot: starting client engine");
        showStatus("Starting the client\u2026");
        loader.setSize(GAME_WIDTH, GAME_HEIGHT);
        client clnt = new client();
        clnt.supplyApplet(loader);
        clnt.init();
        clnt.start();
        Log.i(TAG, "boot: client engine started");
    }

    /**
     * Starts the world server in this process and waits for it to accept
     * connections, mirroring the desktop SinglePlayerLauncher. Launcher.main
     * returns once the world thread is running, so it is called inline.
     *
     * @return false if the world never came up, in which case the client is not
     *         started -- it would only fail to connect.
     */
    private static boolean startWorldServer() {
        Log.i(TAG, "boot: starting world server");
        showStatus("Starting the world\u2026");
        try {
            Launcher.main(new String[0]);
        } catch (Throwable t) {
            Log.e(TAG, "boot: world server failed to start", t);
            showStatus("The world server did not start: " + t);
            return false;
        }
        int port = Settings.getConfig().getWorldInfo().port();
        Log.i(TAG, "boot: waiting for the world on 127.0.0.1:" + port);
        if (!waitForWorld(port)) {
            Log.e(TAG, "boot: the world never opened port " + port);
            showStatus("The world never opened port " + port + ".");
            return false;
        }
        Log.i(TAG, "boot: world server listening on " + port);
        return true;
    }

    private static boolean waitForWorld(int port) {
        for (int attempt = 0; attempt < 100; attempt++) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("127.0.0.1", port), 100);
                return true;
            } catch (Exception notYet) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    private static int lastReportedPercent = -1;

    private static void reportCacheProgress(long bytesDone, long bytesTotal) {
        if (bytesTotal <= 0) return;
        int percent = (int) (bytesDone * 100 / bytesTotal);
        // setPixels-free redraw, but still a surface lock: only repaint on change.
        if (percent == lastReportedPercent) return;
        lastReportedPercent = percent;
        showStatus("Unpacking the game cache\u2026 " + percent + "%");
    }

    private static void showStatus(String message) {
        GameSurfaceView view = GameBridge.surfaceView;
        if (view != null) view.drawStatus(message);
    }

    /**
     * Called by the software renderer (Class158_Sub2_Sub3_Sub1.method14353)
     * each time a frame is presented, to push the ARGB pixel buffer to the
     * Android surface.
     */
    public static void presentFrame(int[] pixels, int width, int height) {
        GameSurfaceView v = GameBridge.surfaceView;
        if (v != null) {
            if (!framePresented) {
                framePresented = true;
                Log.i(TAG, "render: first frame presented " + width + "x" + height);
            }
            v.renderFrame(pixels, width, height);
        }
    }

    private static volatile boolean framePresented;
}
