package astronomicalsystem.app.view;

/**
 * Corner of the image where a block of text is written, in pixels from the top left corner.
 */
public enum Corner {
    TOP_LEFT,
    BOTTOM_LEFT;

    public float left() {
        return 0.0f;
    }

    public float top(float textHeight, float imageHeight) {
        return 0.0f;
    }
}
