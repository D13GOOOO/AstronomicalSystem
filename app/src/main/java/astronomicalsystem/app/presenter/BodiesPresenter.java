package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.Disc;
import astronomicalsystem.app.view.Lighting;
import astronomicalsystem.app.view.SceneView;
import astronomicalsystem.app.view.Surface;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Shows the bodies of the simulation as discs, lit by the brightest star, every time they or the
 * camera move.
 */
public class BodiesPresenter extends ProjectedBodiesPresenter<Disc> {

    private final SceneView view;

    /**
     * @param styles precondition: a style for every kind of body the simulation contains
     */
    public BodiesPresenter(@NotNull SceneView view, @NotNull Projection projection,
                           @NotNull Map<BodyKind, BodyStyle> styles) {
        super(projection, styles);
        this.view = view;
    }

    @Override
    protected @NotNull Disc markOf(@NotNull Body body, @NotNull BodyStyle style, @NotNull ClipPoint centre,
                                   double diameter) {
        return new Disc(centre, diameter, style.colour(), style.shines() ? Lighting.OWN_LIGHT : lightingOf(body),
                Surface.PLAIN);
    }

    // Lit by the most massive body that shines, the brightest star; by its own light if none does
    private @NotNull Lighting lightingOf(@NotNull Body body) {
        return bodies().stream()
                .filter(other -> styleOf(other).shines())
                .max(Comparator.comparingDouble(other -> other.mass().solarMasses()))
                .map(star -> litFrom(star.position().minus(body.position())))
                .orElse(Lighting.OWN_LIGHT);
    }

    // A unit vector in space stays one in the frame of the image, which only turns it
    private @NotNull Lighting litFrom(@NotNull Vector3 towardsTheLight) {
        Vector3 inTheImage = inTheImage(towardsTheLight.normalized());
        return new Lighting.LitFrom(inTheImage.x(), inTheImage.y(), inTheImage.z());
    }

    @Override
    protected void show(@NotNull List<Disc> discs) {
        view.show(discs);
    }
}
