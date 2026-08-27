package java.awt.image;

/**
 * AWT ImageProducer stub. The client does not currently rely on producers.
 */
public interface ImageProducer {
    void addConsumer(ImageConsumer ic);
    boolean isConsumer(ImageConsumer ic);
    void removeConsumer(ImageConsumer ic);
    void startProduction(ImageConsumer ic);
    void requestTopDownLeftRightResend(ImageConsumer ic);
}
