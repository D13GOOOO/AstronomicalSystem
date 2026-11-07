package astronomicalsystem.app.camera;

/**
 * Size of the image a camera produces, in pixels.
 */
public record Viewport(int width, int height) {

    public Viewport {
        if (width <= 0) {
            throw new IllegalArgumentException("Viewport width must be positive");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Viewport height must be positive");
        }
    }

    double aspectRatio() {
        return (double) width / height;
    }
}
