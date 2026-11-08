package astronomicalsystem.app.presenter;

import astronomicalsystem.app.time.TimeControls;
import astronomicalsystem.app.view.KeyboardHandler;
import astronomicalsystem.app.view.KeyboardView;
import org.jetbrains.annotations.NotNull;

/**
 * Lets the user change the flow of simulated time from the keyboard.
 */
public class TimeControlsPresenter implements KeyboardHandler {

    private final TimeControls time;

    public TimeControlsPresenter(@NotNull KeyboardView view, @NotNull TimeControls time) {
        this.time = time;
        view.addHandlers(this);
    }

    @Override
    public void characterTyped(int codePoint) {
        switch (codePoint) {
            case '+', '=' -> time.speedUp();
        }
    }
}
