package astronomicalsystem.app.presenter;

import astronomicalsystem.app.follow.FollowControls;
import astronomicalsystem.app.impact.LaunchControls;
import astronomicalsystem.app.time.TimeControls;
import astronomicalsystem.app.view.KeyboardHandler;
import astronomicalsystem.app.view.KeyboardView;
import org.jetbrains.annotations.NotNull;

/**
 * Lets the user change the flow of simulated time and choose what the camera follows from the
 * keyboard.
 */
public class KeyboardPresenter implements KeyboardHandler {

    private final TimeControls time;
    private final FollowControls follow;
    private final LaunchControls launch;

    public KeyboardPresenter(@NotNull KeyboardView view, @NotNull TimeControls time,
                             @NotNull FollowControls follow, @NotNull LaunchControls launch) {
        this.time = time;
        this.follow = follow;
        this.launch = launch;
        view.addHandlers(this);
    }

    @Override
    public void characterTyped(int codePoint) {
        switch (codePoint) {
            case '+', '=' -> time.speedUp();
            case '-' -> time.slowDown();
            case ' ' -> time.togglePause();
            case '.' -> follow.followNext();
            case ',' -> follow.followPrevious();
        }
    }
}
