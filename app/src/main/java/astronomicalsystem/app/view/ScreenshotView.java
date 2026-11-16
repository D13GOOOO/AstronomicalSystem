package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Keeps a picture of what is on screen.
 */
public interface ScreenshotView {

    /**
     * Writes the next frame, as it will be shown, to the given file.
     */
    void capture(@NotNull Path file);
}
