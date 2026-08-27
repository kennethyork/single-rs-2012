package java.awt;

import java.awt.image.ImageObserver;

/**
 * AWT Canvas shim. The actual Android rendering surface is a GameSurfaceView
 * bridged through AndroidPlatform; this class exists so the client can create
 * an AWT-style canvas host and attach listeners.
 */
public class Canvas extends Component implements ImageObserver {

    public Canvas() {
        setVisible(true);
    }

    @Override
    public Graphics getGraphics() { return null; }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void repaint() {}
}
