package com.rs.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Android SurfaceView that hosts the 2012 client's safe-mode (software) render
 * output. The engine thread writes an int ARGB frame; this view scales it to
 * fill the screen while preserving aspect ratio, and translates touch gestures
 * into the game's mouse model.
 */
public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private volatile boolean surfaceReady = false;
    private Bitmap frameBitmap;
    private int frameWidth;
    private int frameHeight;
    private final Paint paint = new Paint();
    private final Paint statusPaint = new Paint();
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();

    private boolean touchLogged;

    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleDetector;

    // Touch state fed into the game via the input bridge.
    public volatile int gameMouseX;
    public volatile int gameMouseY;
    public volatile int mouseButtonDown;
    public volatile float cameraZoomOffset;
    public volatile int cameraRotationDelta;

    public GameSurfaceView(Context context) {
        this(context, null);
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        paint.setFilterBitmap(false);
        paint.setAntiAlias(false);
        statusPaint.setColor(Color.WHITE);
        statusPaint.setAntiAlias(true);
        statusPaint.setTextAlign(Paint.Align.CENTER);

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                int[] g = screenToGame(e.getX(), e.getY());
                if (g == null) return false;
                gameMouseX = g[0];
                gameMouseY = g[1];
                mouseButtonDown = 1;
                postDelayed(() -> mouseButtonDown = 0, 50);
                tapClient(g[0], g[1], java.awt.event.MouseEvent.BUTTON1);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                int[] g = screenToGame(e.getX(), e.getY());
                if (g == null) return;
                gameMouseX = g[0];
                gameMouseY = g[1];
                mouseButtonDown = 2;
                postDelayed(() -> mouseButtonDown = 0, 100);
                // Long press is the game's right click, for context menus.
                tapClient(g[0], g[1], java.awt.event.MouseEvent.BUTTON3);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
                return true;
            }
        });
        gestureDetector.setIsLongpressEnabled(true);

        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private float zoomAtStart;

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                zoomAtStart = cameraZoomOffset;
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float total = detector.getScaleFactor();
                cameraZoomOffset = Math.max(-250, Math.min(350, zoomAtStart + (1.0f - total) * 300));
                return true;
            }
        });
    }

    /**
     * Sends a mouse event to whatever component the client is listening on.
     *
     * The client registers its mouse listeners on Class351.gameCanvas, and the
     * AWT shim has no event queue to feed them, so touches are delivered here
     * directly. Coordinates are in the client's own frame space, not the view's.
     */
    private void dispatchToClient(int id, int gameX, int gameY, int button) {
        java.awt.Canvas canvas = com.rs.jagex.Class351.gameCanvas;
        if (canvas == null) return;
        canvas.dispatchInputEvent(new java.awt.event.MouseEvent(
                canvas, id, System.currentTimeMillis(), 0, gameX, gameY,
                id == java.awt.event.MouseEvent.MOUSE_CLICKED ? 1 : 0,
                button == java.awt.event.MouseEvent.BUTTON3, button));
    }

    /** A tap: move there, press, release, click -- the sequence AWT would send. */
    private void tapClient(int gameX, int gameY, int button) {
        dispatchToClient(java.awt.event.MouseEvent.MOUSE_MOVED, gameX, gameY,
                java.awt.event.MouseEvent.NOBUTTON);
        dispatchToClient(java.awt.event.MouseEvent.MOUSE_PRESSED, gameX, gameY, button);
        dispatchToClient(java.awt.event.MouseEvent.MOUSE_RELEASED, gameX, gameY, button);
        dispatchToClient(java.awt.event.MouseEvent.MOUSE_CLICKED, gameX, gameY, button);
    }

    /** Called by the engine thread each frame. */
    public void renderFrame(int[] pixels, int width, int height) {
        if (!surfaceReady || width <= 0 || height <= 0) return;
        if (frameBitmap == null || frameBitmap.getWidth() != width || frameBitmap.getHeight() != height) {
            if (frameBitmap != null) frameBitmap.recycle();
            frameBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        frameWidth = width;
        frameHeight = height;
        for (int i = 0; i < pixels.length && i < width * height; i++) {
            pixels[i] = pixels[i] | 0xFF000000;
        }
        frameBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        Canvas canvas = null;
        SurfaceHolder holder = getHolder();
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.BLACK);
                srcRect.set(0, 0, width, height);
                float scaleX = (float) canvas.getWidth() / width;
                float scaleY = (float) canvas.getHeight() / height;
                float scale = Math.min(scaleX, scaleY);
                int dw = (int) (width * scale);
                int dh = (int) (height * scale);
                int dx = (canvas.getWidth() - dw) / 2;
                int dy = (canvas.getHeight() - dh) / 2;
                dstRect.set(dx, dy, dx + dw, dy + dh);
                canvas.drawBitmap(frameBitmap, srcRect, dstRect, paint);
            }
        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * Paints a centred status line. Used before the engine produces frames --
     * chiefly the first-boot cache extraction, which takes a minute or two and
     * would otherwise look like a hang.
     */
    public void drawStatus(String message) {
        if (!surfaceReady) return;
        Canvas canvas = null;
        SurfaceHolder holder = getHolder();
        try {
            canvas = holder.lockCanvas();
            if (canvas == null) return;
            canvas.drawColor(Color.BLACK);
            statusPaint.setTextSize(Math.max(16f, canvas.getWidth() / 28f));
            float y = canvas.getHeight() / 2f - (statusPaint.descent() + statusPaint.ascent()) / 2f;
            canvas.drawText(message, canvas.getWidth() / 2f, y, statusPaint);
        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    private int[] screenToGame(float sx, float sy) {
        int w = getWidth(), h = getHeight();
        int gw = frameWidth > 0 ? frameWidth : 800;
        int gh = frameHeight > 0 ? frameHeight : 600;
        float scaleX = (float) w / gw;
        float scaleY = (float) h / gh;
        float scale = Math.min(scaleX, scaleY);
        int dw = (int) (gw * scale), dh = (int) (gh * scale);
        int dx = (w - dw) / 2, dy = (h - dh) / 2;
        int gx = (int) ((sx - dx) / scale);
        int gy = (int) ((sy - dy) / scale);
        if (gx < 0 || gx >= gw || gy < 0 || gy >= gh) return null;
        return new int[]{gx, gy};
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!touchLogged) {
            touchLogged = true;
            android.util.Log.i(AndroidLoader.TAG, "input: first touch raw=" + event.getX() + ","
                    + event.getY() + " view=" + getWidth() + "x" + getHeight()
                    + " frame=" + frameWidth + "x" + frameHeight);
        }
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                int[] c = screenToGame(event.getX(), event.getY());
                if (c != null) {
                    gameMouseX = c[0];
                    gameMouseY = c[1];
                    dispatchToClient(java.awt.event.MouseEvent.MOUSE_MOVED, c[0], c[1],
                            java.awt.event.MouseEvent.NOBUTTON);
                }
                break;
            case MotionEvent.ACTION_UP:
                mouseButtonDown = 0;
                break;
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) { surfaceReady = true; }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { surfaceReady = true; }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { surfaceReady = false; }
}
