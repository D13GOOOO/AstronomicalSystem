package astronomicalsystem.app.presenter;

import astronomicalsystem.app.screenshot.ScreenshotControls;
import astronomicalsystem.app.view.ScreenshotView;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.time.InstantSource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Asks its view to keep the screen in a file of the pictures folder, named after the local time.
 */
public class ScreenshotPresenter implements ScreenshotControls {

    // Down to the millisecond; sorted by name, the pictures are sorted by time
    private static final DateTimeFormatter FILE_NAME =
            DateTimeFormatter.ofPattern("'AstronomicalSystem-'yyyy-MM-dd-HH-mm-ss-SSS'.png'");

    private final ScreenshotView view;
    private final Path folder;
    private final InstantSource time;
    private final ZoneId zone;
    // The moment the latest picture is named after: the next one is named after a later one
    private LocalDateTime latest = LocalDateTime.MIN;

    public ScreenshotPresenter(@NotNull ScreenshotView view, @NotNull Path folder, @NotNull InstantSource time,
                               @NotNull ZoneId zone) {
        this.view = view;
        this.folder = folder;
        this.time = time;
        this.zone = zone;
    }

    @Override
    public void takeScreenshot() {
        view.capture(folder.resolve(FILE_NAME.format(nameTime())));
    }

    // The local time to the millisecond, or a millisecond after the latest picture if that is
    // not later: keys typed during a slow frame arrive in the same millisecond
    private @NotNull LocalDateTime nameTime() {
        LocalDateTime now = LocalDateTime.ofInstant(time.instant(), zone).truncatedTo(ChronoUnit.MILLIS);
        latest = now.isAfter(latest) ? now : latest.plus(1, ChronoUnit.MILLIS);
        return latest;
    }
}
