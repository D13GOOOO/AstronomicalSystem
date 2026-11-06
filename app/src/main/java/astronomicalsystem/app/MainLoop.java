package astronomicalsystem.app;

import astronomicalsystem.app.time.Clock;
import astronomicalsystem.app.time.Timeline;
import astronomicalsystem.app.view.Window;
import org.jetbrains.annotations.NotNull;

/**
 * Runs the application frame after frame until the window is closed.
 */
public class MainLoop {

    private final Window window;
    private final Clock clock;
    private final Timeline timeline;

    public MainLoop(@NotNull Window window, @NotNull Clock clock, @NotNull Timeline timeline) {
        this.window = window;
        this.clock = clock;
        this.timeline = timeline;
    }

    public void run() {
    }
}
