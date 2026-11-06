package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.Disc;
import astronomicalsystem.app.view.SceneView;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shows the bodies of the simulation as discs, every time they move.
 */
public class BodiesPresenter implements Observer<List<Body>> {

    private final SceneView view;
    private final Projection projection;
    private final Map<BodyKind, BodyStyle> styles;

    /**
     * @param styles precondition: a style for every kind of body the simulation contains
     */
    public BodiesPresenter(@NotNull SceneView view, @NotNull Projection projection,
                           @NotNull Map<BodyKind, BodyStyle> styles) {
        this.view = view;
        this.projection = projection;
        this.styles = Map.copyOf(styles);
    }

    @Override
    public void update(@NotNull Observable<List<Body>> subject, @NotNull List<Body> state) {
        List<Disc> discs = new ArrayList<>();
        for (Body body : state) {
            ClipPoint centre = projection.clipPoint(body.position());
            if (centre.isInFrontOfTheCamera()) {
                discs.add(discOf(body, centre));
            }
        }
        view.show(discs);
    }

    private @NotNull Disc discOf(@NotNull Body body, @NotNull ClipPoint centre) {
        BodyStyle style = styles.get(body.kind());
        double apparentDiameter = projection.apparentSizeInPixels(2.0 * body.radius().astronomicalUnits(), centre);
        return new Disc(centre, Math.max(style.minimumDiameter(), apparentDiameter), style.colour());
    }
}
