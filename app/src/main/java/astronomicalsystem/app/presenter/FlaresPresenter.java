package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.impact.Flare;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.Colour;
import astronomicalsystem.app.view.Glow;
import astronomicalsystem.app.view.GlowsView;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Shows what glows of the impacts as patches of light, of the colour of their heat.
 */
public class FlaresPresenter implements Observer<List<Flare>> {

    // A fireball seen from afar is drawn at least this large, so that the impact can be seen
    private static final double SMALLEST_FIREBALL = 16.0;
    private static final Colour WHITE_HOT = new Colour(1.0, 0.95, 0.85);

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
        flares = List.copyOf(state);
        view.show(flares.stream().map(this::glowOf).toList());
    }

    private @NotNull Glow glowOf(@NotNull Flare flare) {
        ClipPoint centre = projection.clipPoint(flare.position());
        double apparent = projection.apparentSizeInPixels(2.0 * flare.radius(), centre);
        return new Glow(centre, Math.max(SMALLEST_FIREBALL, apparent), WHITE_HOT);
    }
}
