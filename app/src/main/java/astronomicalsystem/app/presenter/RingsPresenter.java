package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.Colour;
import astronomicalsystem.app.view.RingSystem;
import astronomicalsystem.app.view.RingsView;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.rings.Rings;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Shows the rings of the bodies that have them, lit by the brightest star, every time the bodies or
 * the camera move.
 */
public class RingsPresenter extends ProjectedBodiesPresenter<RingSystem> {

    // Seen from where the light comes from, a band that lets nothing through sends back half of
    // the most a band can: there it is shown in its full colour
    private static final double EXPOSURE = 2.0;

    private final RingsView view;
    private final Map<BodyName, Rings> rings;
    private final Colour colour;

    /**
     * @param styles precondition: a style for every kind of body the simulation contains
     * @param rings  the rings of the bodies that have them, by the names of the bodies
     * @param colour of the particles of the rings
     */
    public RingsPresenter(@NotNull RingsView view, @NotNull Projection projection,
                          @NotNull Map<BodyKind, BodyStyle> styles, @NotNull Map<BodyName, Rings> rings,
                          @NotNull Colour colour) {
        super(projection, styles);
        this.view = view;
        this.rings = rings;
        this.colour = colour;
    }

    @Override
    protected boolean isMarked(@NotNull Body body) {
        return rings.containsKey(body.name());
    }

    /**
     * @param body precondition: it has rings
     */
    @Override
    protected @NotNull RingSystem markOf(@NotNull Body body, @NotNull BodyStyle style, @NotNull ClipPoint centre,
                                         double diameter) {
        Rings ringsOfTheBody = rings.get(body.name());
        Vector3 axis = inTheImage(ringsOfTheBody.pole());
        Vector3 towardsTheLight = towardsTheLightOf(body).orElseThrow();
        // The line of sight is the z-axis of the image: its cosine with the axis is the z of the axis
        double towardsViewer = Math.abs(axis.z());
        double towardsLight = Math.abs(axis.dot(towardsTheLight));
        double radius = body.radius().astronomicalUnits();
        List<RingSystem.Band> bands = ringsOfTheBody.bands().stream()
                .map(band -> new RingSystem.Band(band.inner().astronomicalUnits() / radius,
                        band.outer().astronomicalUnits() / radius,
                        lit(EXPOSURE * band.reflected(towardsViewer, towardsLight)),
                        band.opacity(towardsViewer)))
                .toList();
        return new RingSystem(centre, diameter, axis, towardsTheLight, bands);
    }

    // The colour of the rings at the brightness given, 1 being the colour itself
    private @NotNull Colour lit(double brightness) {
        return new Colour(colour.red() * brightness, colour.green() * brightness, colour.blue() * brightness);
    }

    @Override
    protected void show(@NotNull List<RingSystem> ringSystems) {
        view.show(ringSystems);
    }
}
