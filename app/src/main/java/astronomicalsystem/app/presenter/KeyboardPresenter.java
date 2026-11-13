package astronomicalsystem.app.presenter;

import astronomicalsystem.app.follow.FollowControls;
import astronomicalsystem.app.time.TimeControls;
import astronomicalsystem.app.view.KeyboardHandler;
import astronomicalsystem.app.view.KeyboardView;
import org.jetbrains.annotations.NotNull;

/**
 * Lets the user change the flow of simulated time from the keyboard.
 */
public class KeyboardPresenter implements KeyboardHandler {

    private final TimeControls time;

    public KeyboardPresenter(@NotNull KeyboardView view, @NotNull TimeControls time,
                             @NotNull FollowControls follow) {
        this.time = time;
        view.addHandlers(this);
    }

    @Override
    public void characterTyped(int codePoint) {
        switch (codePoint) {
            case '+', '=' -> time.speedUp();
            case '-' -> time.slowDown();
            case ' ' -> time.togglePause();
        }
    }
}
