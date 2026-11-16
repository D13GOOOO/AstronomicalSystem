package astronomicalsystem.app.presenter;

import astronomicalsystem.app.screenshot.ScreenshotControls;
import astronomicalsystem.app.view.ScreenshotView;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.time.Clock;

/**
 * Asks its view to keep the screen in a file of the pictures folder, named after the local time.
 */
public class ScreenshotPresenter implements ScreenshotControls {

    public ScreenshotPresenter(@NotNull ScreenshotView view, @NotNull Path folder, @NotNull Clock clock) {
    }

    @Override
    public void takeScreenshot() {
    }
}
