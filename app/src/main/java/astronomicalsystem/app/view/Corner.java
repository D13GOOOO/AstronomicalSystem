package astronomicalsystem.app.view;

/**
 * Corner of the image where a block of text is written, a margin away from its edges; positions
 * in pixels from the top left corner of the image.
 */
public enum Corner {
    TOP_LEFT(Side.NEAR, Side.NEAR),
    BOTTOM_LEFT(Side.NEAR, Side.FAR),
    BOTTOM_RIGHT(Side.FAR, Side.FAR),
    TOP_RIGHT(Side.FAR, Side.NEAR);

    private static final float MARGIN_IN_PIXELS = 10.0f;

    private final Side horizontal;
    private final Side vertical;

    Corner(Side horizontal, Side vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public float left(float textWidth, float imageWidth) {
        return horizontal.start(textWidth, imageWidth);
    }

    public float top(float textHeight, float imageHeight) {
        return vertical.start(textHeight, imageHeight);
    }

    /**
     * Side of the image, along one of its axes, that the text keeps close to: the near one is the
     * left or the top.
     */
    private enum Side {
        NEAR {
            @Override
            float start(float textSize, float imageSize) {
                return MARGIN_IN_PIXELS;
            }
        },
        FAR {
            @Override
            float start(float textSize, float imageSize) {
                return imageSize - MARGIN_IN_PIXELS - textSize;
            }
        };

        abstract float start(float textSize, float imageSize);
    }
}
