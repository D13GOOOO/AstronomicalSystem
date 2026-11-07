package astronomicalsystem.app.view;

/**
 * Receives what the user does in a window, as raw events.
 */
public interface InputHandler {

    /**
     * The main button was pressed with the pointer at the given position, in pixels from the top
     * left corner of the window.
     */
    void pointerPressed(double x, double y);

    void pointerReleased();

    /**
     * @param x pixels from the left edge of the window
     * @param y pixels from the top edge of the window
     */
    void pointerMoved(double x, double y);

    /**
     * @param steps of the wheel, positive away from the user
     */
    void scrolled(double steps);

    /**
     * @param width  of the image, in pixels; 0 when the window is minimised
     * @param height of the image, in pixels; 0 when the window is minimised
     */
    void resized(int width, int height);
}
