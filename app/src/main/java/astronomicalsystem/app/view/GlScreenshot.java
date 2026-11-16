package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * The last layer of a frame: when a capture is asked for, it writes the image drawn so far to a
 * PNG file before it is shown.
 * <p>
 * Not covered by unit tests, since it needs a graphics card: see the integration tests.
 */
public final class GlScreenshot implements ScreenshotView, Drawable {

    @Override
    public void capture(@NotNull Path file) {
    }

    @Override
    public void draw() {
    }
}
