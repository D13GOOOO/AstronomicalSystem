package astronomicalsystem.app.presenter;

import astronomicalsystem.app.time.TimeControls;
import astronomicalsystem.app.view.KeyboardHandler;
import astronomicalsystem.app.view.KeyboardView;
import org.jetbrains.annotations.NotNull;

/**
 * Lets the user change the flow of simulated time from the keyboard.
 */
public class TimeControlsPresenter implements KeyboardHandler {

    public TimeControlsPresenter(@NotNull KeyboardView view, @NotNull TimeControls time) {
    }

    @Override
    public void characterTyped(int codePoint) {
    }
}
