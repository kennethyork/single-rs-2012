package com.rs.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

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
    private boolean keyboardVisible;
    private boolean cameraDragging;
    /** Pinch scale since the last emitted wheel notch. */
    private float pinchAccumulator = 1f;
    /** How far a pinch must travel to count as one wheel notch. */
    private static final float PINCH_NOTCH = 1.15f;

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
        requestFocus();
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
                // A drag scrolls whatever list is under it, as the wheel would.
                dispatchWheelToClient(gameMouseX, gameMouseY, dy > 0 ? 1 : -1);
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // The client draws its own login and chat fields, so Android has
                // nothing to focus and no reason to raise the keyboard on its
                // own. Double tap toggles it.
                if (keyboardVisible) hideKeyboard(); else showKeyboard();
                keyboardVisible = !keyboardVisible;
                return true;
            }
        });
        gestureDetector.setIsLongpressEnabled(true);

        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private float zoomAtStart;

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                zoomAtStart = cameraZoomOffset;
                pinchAccumulator = 1f;
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float total = detector.getScaleFactor();
                cameraZoomOffset = Math.max(-250, Math.min(350, zoomAtStart + (1.0f - total) * 300));
                // Turn the continuous pinch into discrete wheel notches, which
                // is all the client understands.
                pinchAccumulator *= total;
                while (pinchAccumulator >= PINCH_NOTCH) {
                    pinchAccumulator /= PINCH_NOTCH;
                    dispatchWheelToClient(gameMouseX, gameMouseY, -1);   // spread fingers = zoom in
                }
                while (pinchAccumulator <= 1f / PINCH_NOTCH) {
                    pinchAccumulator *= PINCH_NOTCH;
                    dispatchWheelToClient(gameMouseX, gameMouseY, 1);
                }
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

    /**
     * Sends a mouse wheel notch. The client zooms the camera on wheel movement
     * over the world, and scrolls whatever interface is under the pointer
     * otherwise, so a pinch drives both without having to know which is which.
     */
    private void dispatchWheelToClient(int gameX, int gameY, int notches) {
        java.awt.Canvas canvas = com.rs.jagex.Class351.gameCanvas;
        if (canvas == null || notches == 0) return;
        canvas.dispatchInputEvent(new java.awt.event.MouseWheelEvent(
                canvas, java.awt.event.MouseEvent.MOUSE_WHEEL, System.currentTimeMillis(), 0,
                gameX, gameY, 0, false,
                java.awt.event.MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, notches));
    }

    /** A tap: move there, press, release, click -- the sequence AWT would send. */
    private void tapClient(int gameX, int gameY, int button) {
        dispatchToClient(java.awt.event.MouseEvent.MOUSE_MOVED, gameX, gameY,
                java.awt.event.MouseEvent.NOBUTTON);
        dispatchToClient(java.awt.event.MouseEvent.MOUSE_PRESSED, gameX, gameY, button);
        dispatchToClient(java.awt.event.MouseEvent.MOUSE_RELEASED, gameX, gameY, button);
        dispatchToClient(java.awt.event.MouseEvent.MOUSE_CLICKED, gameX, gameY, button);
    }

    /**
     * Declares this view as a text target and intercepts what the IME produces.
     *
     * TYPE_NULL, which makes a keyboard fall back to raw key events, is not
     * enough: modern soft keyboards commit text through commitText and
     * deleteSurroundingText and never send a KeyEvent at all. Relying on key
     * events works for a hardware keyboard and for `adb shell input text`, which
     * is why it passed on the emulator and would still have done nothing on a
     * phone. Same approach as Single-RSC Mobile.
     *
     * VISIBLE_PASSWORD with NO_SUGGESTIONS keeps the IME from autocorrecting or
     * composing over what is typed, since the client owns the text, not Android.
     */
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
                | EditorInfo.IME_FLAG_NO_FULLSCREEN
                | EditorInfo.IME_ACTION_DONE;
        return new GameInputConnection(this);
    }

    /** Routes everything an IME can produce into the client's key handling. */
    private class GameInputConnection extends BaseInputConnection {
        GameInputConnection(android.view.View target) {
            super(target, false);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            if (text != null)
                for (int i = 0; i < text.length(); i++) typeChar(text.charAt(i));
            return true;
        }

        @Override
        public boolean setComposingText(CharSequence text, int newCursorPosition) {
            // The client has no notion of composing text; treat it as committed.
            return commitText(text, newCursorPosition);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            for (int i = 0; i < beforeLength; i++)
                typeKey(java.awt.event.KeyEvent.VK_BACK_SPACE, '\b');
            return true;
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            // Some keyboards do still send key events.
            if (event.getAction() == KeyEvent.ACTION_DOWN)
                return onKeyDown(event.getKeyCode(), event);
            if (event.getAction() == KeyEvent.ACTION_UP)
                return onKeyUp(event.getKeyCode(), event);
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean performEditorAction(int actionCode) {
            typeKey(java.awt.event.KeyEvent.VK_ENTER, '\n');   // the IME's Done key
            return true;
        }
    }

    /** A printable character: the client reads these through AWT keyTyped. */
    private void typeChar(char c) {
        java.awt.Canvas canvas = com.rs.jagex.Class351.gameCanvas;
        if (canvas == null || c == 0) return;
        canvas.dispatchInputEvent(new java.awt.event.KeyEvent(
                canvas, java.awt.event.KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0,
                0, c, 0));
    }

    /** A named key: pressed, typed, released, as AWT would deliver it. */
    private void typeKey(int awtKeyCode, char keyChar) {
        java.awt.Canvas canvas = com.rs.jagex.Class351.gameCanvas;
        if (canvas == null) return;
        long now = System.currentTimeMillis();
        canvas.dispatchInputEvent(new java.awt.event.KeyEvent(
                canvas, java.awt.event.KeyEvent.KEY_PRESSED, now, 0, awtKeyCode,
                (char) java.awt.event.KeyEvent.CHAR_UNDEFINED, 0));
        canvas.dispatchInputEvent(new java.awt.event.KeyEvent(
                canvas, java.awt.event.KeyEvent.KEY_TYPED, now, 0, 0, keyChar, 0));
        canvas.dispatchInputEvent(new java.awt.event.KeyEvent(
                canvas, java.awt.event.KeyEvent.KEY_RELEASED, now, 0, awtKeyCode,
                (char) java.awt.event.KeyEvent.CHAR_UNDEFINED, 0));
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    /** Raises the soft keyboard, for the login form and chat. */
    public void showKeyboard() {
        requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        // SHOW_FORCED rather than SHOW_IMPLICIT: nothing Android recognises as a
        // text field is focused, and implicit requests are often ignored.
        if (imm != null) imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
    }

    /** Hides the soft keyboard. */
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Leave the system keys alone: back should still leave the game.
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)
            return super.onKeyDown(keyCode, event);
        dispatchKeyToClient(java.awt.event.KeyEvent.KEY_PRESSED, keyCode, event);
        int typed = event.getUnicodeChar(event.getMetaState());
        if (typed != 0)
            dispatchKeyToClient(java.awt.event.KeyEvent.KEY_TYPED, keyCode, event);
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)
            return super.onKeyUp(keyCode, event);
        dispatchKeyToClient(java.awt.event.KeyEvent.KEY_RELEASED, keyCode, event);
        return true;
    }

    private void dispatchKeyToClient(int id, int androidKeyCode, KeyEvent event) {
        java.awt.Canvas canvas = com.rs.jagex.Class351.gameCanvas;
        if (canvas == null) return;
        int unicode = event.getUnicodeChar(event.getMetaState());
        // keyTyped carries the character; keyPressed/keyReleased carry the code,
        // which the client uses to index a lookup table of AWT VK values.
        char keyChar = id == java.awt.event.KeyEvent.KEY_TYPED && unicode != 0
                ? (char) unicode : (char) java.awt.event.KeyEvent.CHAR_UNDEFINED;
        int awtCode = id == java.awt.event.KeyEvent.KEY_TYPED ? 0 : awtKeyCode(androidKeyCode, unicode);
        canvas.dispatchInputEvent(new java.awt.event.KeyEvent(
                canvas, id, System.currentTimeMillis(), 0, awtCode, keyChar, 0));
    }

    /**
     * Android key code to AWT virtual key code.
     *
     * AWT numbers letters and digits by their ASCII uppercase value, so most keys
     * fall out of the character the event carries; only the named keys need a
     * table.
     */
    private static int awtKeyCode(int androidKeyCode, int unicode) {
        switch (androidKeyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER: return java.awt.event.KeyEvent.VK_ENTER;
            case KeyEvent.KEYCODE_DEL:          return java.awt.event.KeyEvent.VK_BACK_SPACE;
            case KeyEvent.KEYCODE_TAB:          return java.awt.event.KeyEvent.VK_TAB;
            case KeyEvent.KEYCODE_ESCAPE:       return java.awt.event.KeyEvent.VK_ESCAPE;
            case KeyEvent.KEYCODE_SPACE:        return java.awt.event.KeyEvent.VK_SPACE;
            case KeyEvent.KEYCODE_DPAD_LEFT:    return java.awt.event.KeyEvent.VK_LEFT;
            case KeyEvent.KEYCODE_DPAD_UP:      return java.awt.event.KeyEvent.VK_UP;
            case KeyEvent.KEYCODE_DPAD_RIGHT:   return java.awt.event.KeyEvent.VK_RIGHT;
            case KeyEvent.KEYCODE_DPAD_DOWN:    return java.awt.event.KeyEvent.VK_DOWN;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:  return java.awt.event.KeyEvent.VK_SHIFT;
            case KeyEvent.KEYCODE_CTRL_LEFT:
            case KeyEvent.KEYCODE_CTRL_RIGHT:   return java.awt.event.KeyEvent.VK_CONTROL;
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:    return java.awt.event.KeyEvent.VK_ALT;
            default: break;
        }
        if (unicode >= 'a' && unicode <= 'z') return unicode - ('a' - 'A');
        if (unicode >= 'A' && unicode <= 'Z') return unicode;
        if (unicode >= '0' && unicode <= '9') return unicode;
        return 0;
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

    /**
     * Two-finger drag rotates the camera.
     *
     * The client rotates while the middle mouse button is held and the pointer
     * moves (Class209_Sub1.middleButtonDown), so the gesture is delivered as a
     * middle-button drag rather than anything Android-specific.
     */
    private void beginCameraDrag(MotionEvent event) {
        int[] g = screenToGame(centroidX(event), centroidY(event));
        if (g == null) return;
        cameraDragging = true;
        dispatchToClient(java.awt.event.MouseEvent.MOUSE_PRESSED, g[0], g[1],
                java.awt.event.MouseEvent.BUTTON2);
    }

    private void endCameraDrag() {
        if (!cameraDragging) return;
        cameraDragging = false;
        dispatchToClient(java.awt.event.MouseEvent.MOUSE_RELEASED, gameMouseX, gameMouseY,
                java.awt.event.MouseEvent.BUTTON2);
    }

    private static float centroidX(MotionEvent event) {
        float sum = 0;
        for (int i = 0; i < event.getPointerCount(); i++) sum += event.getX(i);
        return sum / event.getPointerCount();
    }

    private static float centroidY(MotionEvent event) {
        float sum = 0;
        for (int i = 0; i < event.getPointerCount(); i++) sum += event.getY(i);
        return sum / event.getPointerCount();
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
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) beginCameraDrag(event);
                break;
            case MotionEvent.ACTION_MOVE:
                boolean twoFinger = cameraDragging && event.getPointerCount() >= 2;
                int[] c = twoFinger
                        ? screenToGame(centroidX(event), centroidY(event))
                        : screenToGame(event.getX(), event.getY());
                if (c != null) {
                    gameMouseX = c[0];
                    gameMouseY = c[1];
                    dispatchToClient(twoFinger
                                    ? java.awt.event.MouseEvent.MOUSE_DRAGGED
                                    : java.awt.event.MouseEvent.MOUSE_MOVED,
                            c[0], c[1],
                            twoFinger ? java.awt.event.MouseEvent.BUTTON2
                                    : java.awt.event.MouseEvent.NOBUTTON);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() <= 2) endCameraDrag();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                endCameraDrag();
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
