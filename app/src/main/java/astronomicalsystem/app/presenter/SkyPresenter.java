package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.sky.Star;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.Colour;
import astronomicalsystem.app.view.Glow;
import astronomicalsystem.app.view.GlowsView;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Shows the stars of the background sky as points of light, where every new camera sees them.
 */
public class SkyPresenter implements Observer<Projection> {

    // In AU: so far that the stars do not move in the image when the camera moves across the system
    private static final double DISTANCE_OF_THE_STARS = 1e9;
    // The sky is drawn first and everything else over it: any depth that can be seen will do
    private static final double DEPTH_OF_THE_SKY = 0.0;
    // In pixels
    private static final double POINT_OF_LIGHT = 3.0;
    // How bright the faintest star is drawn, 1 being the brightest the image can show
    private static final double LIGHT_OF_THE_FAINTEST = 0.125;
    private static final double FULL_LIGHT = 1.0;

    private final GlowsView view;
    private final List<Star> stars;

    public SkyPresenter(@NotNull GlowsView view, @NotNull List<Star> stars) {
        this.view = view;
        this.stars = stars;
    }

    @Override
    public void update(@NotNull Observable<Projection> subject, @NotNull Projection camera) {
        view.show(stars.stream()
                .filter(star -> whereSeen(star, camera).isInFrontOfTheCamera())
                .map(star -> glowOf(star, camera))
                .toList());
    }

    private static @NotNull ClipPoint whereSeen(@NotNull Star star, @NotNull Projection camera) {
        return camera.clipPoint(star.direction().times(DISTANCE_OF_THE_STARS));
    }

    /**
     * @param star precondition: in front of the camera
     */
    private static @NotNull Glow glowOf(@NotNull Star star, @NotNull Projection camera) {
        ClipPoint seen = whereSeen(star, camera);
        double light = LIGHT_OF_THE_FAINTEST * star.flux();
        double shown = Math.min(FULL_LIGHT, light);
        // The light that a point cannot show is spread around it, as on a photograph: the light of
        // each pixel times the area stays in proportion to the flux
        double diameter = POINT_OF_LIGHT * Math.sqrt(light / shown);
        return new Glow(new ClipPoint(seen.x(), seen.y(), DEPTH_OF_THE_SKY, seen.w()), diameter,
                new Colour(shown, shown, shown));
    }
}
