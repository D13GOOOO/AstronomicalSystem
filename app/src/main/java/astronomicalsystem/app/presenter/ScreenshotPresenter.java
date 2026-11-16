package astronomicalsystem.app.presenter;

import astronomicalsystem.app.screenshot.ScreenshotControls;
import astronomicalsystem.app.view.ScreenshotView;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Asks its view to keep the screen in a file of the pictures folder, named after the local time.
 */
public class ScreenshotPresenter implements ScreenshotControls {

    // Down to the millisecond, so that two pictures never share a name; sorted by name, by time
    private static final DateTimeFormatter FILE_NAME =
            DateTimeFormatter.ofPattern("'AstronomicalSystem-'yyyy-MM-dd-HH-mm-ss-SSS'.png'");

    private final ScreenshotView view;
    private final Path folder;
    private final Clock clock;

    public ScreenshotPresenter(@NotNull ScreenshotView view, @NotNull Path folder, @NotNull Clock clock) {
        this.view = view;
        this.folder = folder;
        this.clock = clock;
    }

    @Override
    public void takeScreenshot() {
        view.capture(folder.resolve(fileName()));
    }

    private @NotNull String fileName() {
        return FILE_NAME.format(LocalDateTime.now(clock));
    }
}
