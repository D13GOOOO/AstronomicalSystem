package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.sky.Star;
import astronomicalsystem.app.view.GlowsView;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Shows the stars of the background sky as points of light, where every new camera sees them.
 */
public class SkyPresenter implements Observer<Projection> {

    private final GlowsView view;
    private final List<Star> stars;

    public SkyPresenter(@NotNull GlowsView view, @NotNull List<Star> stars) {
        this.view = view;
        this.stars = stars;
    }

    @Override
    public void update(@NotNull Observable<Projection> subject, @NotNull Projection camera) {
    }
}
