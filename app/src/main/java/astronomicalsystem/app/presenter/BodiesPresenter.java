package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.time.TimeFlow;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.Disc;
import astronomicalsystem.app.view.Lighting;
import astronomicalsystem.app.view.SceneView;
import astronomicalsystem.app.view.Surface;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.rotation.Axes;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shows the bodies of the simulation as discs, lit by the brightest star and wrapped in the maps of
 * their surfaces, every time they move, the camera moves or time goes by.
 */
public class BodiesPresenter extends ProjectedBodiesPresenter<Disc> {

    private final SceneView view;
    private final Map<BodyName, Skin> skins;
    private JulianDate date;

    /**
     * @param styles precondition: a style for every kind of body the simulation contains
     * @param skins  the bodies that have a map of their surface, by their names; the others are
     *               drawn in the colour of their kind
     * @param start  the date of the simulation when it begins
     */
    public BodiesPresenter(@NotNull SceneView view, @NotNull Projection projection,
                           @NotNull Map<BodyKind, BodyStyle> styles, @NotNull Map<BodyName, Skin> skins,
                           @NotNull JulianDate start) {
        super(projection, styles);
        this.view = view;
        this.skins = Map.copyOf(skins);
        this.date = start;
    }

    /**
     * @return the observer that turns the maps of the bodies as simulated time goes by
     */
    public @NotNull Observer<TimeFlow> timeObserver() {
        return (subject, flow) -> {
            date = flow.date();
            redraw();
        };
    }

    @Override
    protected @NotNull Disc markOf(@NotNull Body body, @NotNull BodyStyle style, @NotNull ClipPoint centre,
                                   double diameter) {
        return new Disc(centre, diameter, style.colour(), style.shines() ? Lighting.OWN_LIGHT : lightingOf(body),
                surfaceOf(body));
    }

    // The map of the body turned as the body is now, if it has one
    private @NotNull Surface surfaceOf(@NotNull Body body) {
        return Optional.ofNullable(skins.get(body.name())).map(this::mapped).orElse(Surface.PLAIN);
    }

    private @NotNull Surface mapped(@NotNull Skin skin) {
        Axes axes = skin.spin().axesAt(date);
        return new Surface.Mapped(skin.texture(), inTheImage(axes.x()), inTheImage(axes.y()), inTheImage(axes.z()));
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
