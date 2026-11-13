package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.ImagePoint;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shows a mark for every body of the simulation in front of the camera, every time the bodies or
 * the camera move; subclasses decide what the mark is and where it is shown.
 *
 * @param <M> the mark of a body
 */
public abstract class ProjectedBodiesPresenter<M> implements Observer<List<Body>> {

    private final Map<BodyKind, BodyStyle> styles;
    // The latest camera and the latest bodies: a change of either shows the marks again
    private Projection projection;
    private List<Body> bodies = List.of();

    /**
     * @param styles precondition: a style for every kind of body the simulation contains
     */
    protected ProjectedBodiesPresenter(@NotNull Projection projection, @NotNull Map<BodyKind, BodyStyle> styles) {
        this.projection = projection;
        this.styles = Map.copyOf(styles);
    }

    @Override
    public final void update(@NotNull Observable<List<Body>> subject, @NotNull List<Body> state) {
        bodies = List.copyOf(state);
        redraw();
    }

    /**
     * @return the observer that shows the marks from the point of view of every new camera
     */
    public final @NotNull Observer<Projection> cameraObserver() {
        return (subject, camera) -> {
            projection = camera;
            redraw();
        };
    }

    /**
     * @param style    how bodies of its kind are drawn
     * @param centre   precondition: in front of the camera
     * @param diameter of the disc of the body, in pixels
     */
    protected abstract @NotNull M markOf(@NotNull Body body, @NotNull BodyStyle style, @NotNull ClipPoint centre,
                                         double diameter);

    protected abstract void show(@NotNull List<M> marks);

    /**
     * @param point precondition: in front of the camera
     */
    protected final @NotNull ImagePoint imagePointOf(@NotNull ClipPoint point) {
        return projection.imagePoint(point);
    }

    private void redraw() {
        List<M> marks = new ArrayList<>();
        for (Body body : bodies) {
            ClipPoint centre = projection.clipPoint(body.position());
            if (centre.isInFrontOfTheCamera()) {
                BodyStyle style = styles.get(body.kind());
                marks.add(markOf(body, style, centre, diameterOf(body, style, centre)));
            }
        }
        show(marks);
    }

    // At least the minimum of the style, since at the scale of the solar system bodies are smaller
    // than a pixel
    private double diameterOf(@NotNull Body body, @NotNull BodyStyle style, @NotNull ClipPoint centre) {
        double apparentDiameter = projection.apparentSizeInPixels(2.0 * body.radius().astronomicalUnits(), centre);
        return Math.max(style.minimumDiameter(), apparentDiameter);
    }
}
