package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.ImagePoint;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shows a mark for every body of the simulation in front of the camera, every time the bodies or
 * the camera move; subclasses decide what the mark is and where it is shown.
 *
 * @param <M> the mark of a body
 */
public abstract class ProjectedBodiesPresenter<M> extends ProjectingPresenter<Body> {

    private final Map<BodyKind, BodyStyle> styles;

    /**
     * @param styles precondition: a style for every kind of body the simulation contains
     */
    protected ProjectedBodiesPresenter(@NotNull Projection projection, @NotNull Map<BodyKind, BodyStyle> styles) {
        super(projection);
        this.styles = Map.copyOf(styles);
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
        return projection().imagePoint(point);
    }

    /**
     * @return the direction in the frame of the image of the latest camera
     */
    protected final @NotNull Vector3 inTheImage(@NotNull Vector3 direction) {
        return projection().inTheImage(direction);
    }

    /**
     * @return the unit vector, in the frame of the image, from the body towards the most massive
     *         body that shines, the brightest star; empty if no body shines
     */
    protected final @NotNull Optional<Vector3> towardsTheLightOf(@NotNull Body body) {
        // A unit vector in space stays one in the frame of the image, which only turns it
        return latest().stream()
                .filter(other -> styleOf(other).shines())
                .max(Comparator.comparingDouble(other -> other.mass().solarMasses()))
                .map(star -> inTheImage(star.position().minus(body.position()).normalized()));
    }

    protected final @NotNull BodyStyle styleOf(@NotNull Body body) {
        return styles.get(body.kind());
    }

    @Override
    protected final void redraw() {
        List<M> marks = new ArrayList<>();
        for (Body body : latest()) {
            ClipPoint centre = projection().clipPoint(body.position());
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
        double apparentDiameter = projection().apparentSizeInPixels(2.0 * body.radius().astronomicalUnits(), centre);
        return Math.max(style.minimumDiameter(), apparentDiameter);
    }
}
