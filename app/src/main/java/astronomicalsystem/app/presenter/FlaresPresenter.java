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

    // A fireball seen from afar is drawn at least this large, so that the impact can be seen; an
    // ember, which has no size, is a spark
    private static final double SMALLEST_FIREBALL = 16.0;
    private static final double SPARK = 4.0;
    // Glowing matter cools from white through orange and red to dark, as a black body does; the
    // colours at these heats, and in between a mix of the two around
    private static final double[] HEATS = {0.0, 0.25, 0.5, 1.0};
    private static final Colour[] COLOURS = {new Colour(0.0, 0.0, 0.0), new Colour(0.8, 0.25, 0.05),
            new Colour(1.0, 0.6, 0.2), new Colour(1.0, 0.95, 0.85)};

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
        view.show(flares.stream()
                .filter(flare -> projection.clipPoint(flare.position()).isInFrontOfTheCamera())
                .map(this::glowOf)
                .toList());
    }

    /**
     * @return the observer that shows the flares from the point of view of every new camera
     */
    public @NotNull Observer<Projection> cameraObserver() {
        return (subject, camera) -> {
        };
    }

    /**
     * @param flare precondition: in front of the camera
     */
    private @NotNull Glow glowOf(@NotNull Flare flare) {
        ClipPoint centre = projection.clipPoint(flare.position());
        double apparent = projection.apparentSizeInPixels(2.0 * flare.radius(), centre);
        double smallest = flare.radius() > 0.0 ? SMALLEST_FIREBALL : SPARK;
        return new Glow(centre, Math.max(smallest, apparent), colourOf(flare.heat()));
    }

    // Linear between the two colours around the heat
    private static @NotNull Colour colourOf(double heat) {
        int upper = 1;
        while (upper < HEATS.length - 1 && HEATS[upper] < heat) {
            upper++;
        }
        double share = (heat - HEATS[upper - 1]) / (HEATS[upper] - HEATS[upper - 1]);
        Colour cooler = COLOURS[upper - 1];
        Colour hotter = COLOURS[upper];
        return new Colour(mix(cooler.red(), hotter.red(), share), mix(cooler.green(), hotter.green(), share),
                mix(cooler.blue(), hotter.blue(), share));
    }

    // Exactly one or the other at the ends
    private static double mix(double from, double to, double share) {
        return from * (1.0 - share) + to * share;
    }
}
