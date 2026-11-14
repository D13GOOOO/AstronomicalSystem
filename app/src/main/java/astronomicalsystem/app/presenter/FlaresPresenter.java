package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.impact.Flare;
import astronomicalsystem.app.view.GlowsView;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Shows what glows of the impacts as patches of light, of the colour of their heat.
 */
public class FlaresPresenter implements Observer<List<Flare>> {

    private final GlowsView view;
    // The latest camera and the latest flares: a change of either shows them again
    private Projection projection;
    private List<Flare> flares = List.of();

    public FlaresPresenter(@NotNull GlowsView view, @NotNull Projection projection) {
        this.view = view;
        this.projection = projection;
    }

    @Override
    public void update(@NotNull Observable<List<Flare>> subject, @NotNull List<Flare> state) {
    }
}
