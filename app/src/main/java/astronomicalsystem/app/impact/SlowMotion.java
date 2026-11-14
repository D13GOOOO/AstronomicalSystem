package astronomicalsystem.app.impact;

import astronomicalsystem.app.follow.Focus;
import astronomicalsystem.app.follow.Following;
import astronomicalsystem.app.time.TimeControls;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.physics.Collision;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Slows time down as much as it can when the body the camera follows is hit, so that the user
 * sees the impact unfold.
 */
public class SlowMotion implements Observer<List<Collision>> {

    private final Following following;
    private final TimeControls time;

    public SlowMotion(@NotNull Following following, @NotNull TimeControls time) {
        this.following = following;
        this.time = time;
    }

    @Override
    public void update(@NotNull Observable<List<Collision>> subject, @NotNull List<Collision> state) {
        Focus focus = following.focus();
        if (state.stream().anyMatch(collision -> new Focus.OnBody(collision.survivor()).equals(focus))) {
            time.slowest();
        }
    }
}
