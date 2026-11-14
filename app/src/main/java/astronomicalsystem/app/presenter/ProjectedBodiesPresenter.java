package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.ImagePoint;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.math.Vector3;
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
     * @return whether the body gets a mark when it is in front of the camera; every body, unless a
     *         subclass leaves some out
     */
    protected boolean isMarked(@NotNull Body body) {
        return true;
    }

    /**
     * @param point precondition: in front of the camera
     */
    protected final @NotNull ImagePoint imagePointOf(@NotNull ClipPoint point) {
        return projection.imagePoint(point);
    }

    /**
     * @return the direction in the frame of the image of the latest camera
     */
    protected final @NotNull Vector3 inTheImage(@NotNull Vector3 direction) {
        return projection.inTheImage(direction);
    }

    /**
     * @return the latest bodies, all of them, also those not in front of the camera
     */
    protected final @NotNull List<Body> bodies() {
        return bodies;
    }

    protected final @NotNull BodyStyle styleOf(@NotNull Body body) {
        return styles.get(body.kind());
    }

    private void redraw() {
        List<M> marks = new ArrayList<>();
        for (Body body : bodies) {
            ClipPoint centre = projection.clipPoint(body.position());
            if (isMarked(body) && centre.isInFrontOfTheCamera()) {
                BodyStyle style = styleOf(body);
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
