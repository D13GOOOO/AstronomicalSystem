package astronomicalsystem.app.view;

/**
 * Corner of the image where a block of text is written, a margin away from its edges; positions
 * in pixels from the top left corner of the image.
 */
public enum Corner {
    TOP_LEFT {
        @Override
        public float top(float textHeight, float imageHeight) {
            return MARGIN_IN_PIXELS;
        }
    },
    BOTTOM_LEFT {
        @Override
        public float top(float textHeight, float imageHeight) {
            return imageHeight - MARGIN_IN_PIXELS - textHeight;
        }
    };

    private static final float MARGIN_IN_PIXELS = 10.0f;

    public float left(float textWidth, float imageWidth) {
        return MARGIN_IN_PIXELS;
    }

    public abstract float top(float textHeight, float imageHeight);
}
