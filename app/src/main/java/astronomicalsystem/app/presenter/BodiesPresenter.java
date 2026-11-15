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
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.rotation.Axes;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Shows the bodies of the simulation as discs, lit by the brightest star and wrapped in the maps of
 * their surfaces, every time they move, the camera moves or time goes by.
 */
public class BodiesPresenter extends ProjectedBodiesPresenter<Disc> {

    private final SceneView view;
    private final Skins skins;
    private JulianDate date;

    /**
     * @param styles precondition: a style for every kind of body the simulation contains
     * @param skins  the maps of the surfaces of the bodies that have one; the others are drawn in
     *               the colour of their kind
     * @param start  the date of the simulation when it begins
     */
    public BodiesPresenter(@NotNull SceneView view, @NotNull Projection projection,
                           @NotNull Map<BodyKind, BodyStyle> styles, @NotNull Skins skins,
                           @NotNull JulianDate start) {
        super(projection, styles);
        this.view = view;
        this.skins = skins;
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
        return skins.of(body).map(this::mapped).orElse(Surface.PLAIN);
    }

    private @NotNull Surface mapped(@NotNull Skin skin) {
        Axes axes = skin.spin().axesAt(date);
        return new Surface.Mapped(skin.texture(), inTheImage(axes.x()), inTheImage(axes.y()), inTheImage(axes.z()));
    }

    // Lit by the brightest star; by its own light if no body shines
    private @NotNull Lighting lightingOf(@NotNull Body body) {
        return towardsTheLightOf(body)
                .<Lighting>map(light -> new Lighting.LitFrom(light.x(), light.y(), light.z()))
                .orElse(Lighting.OWN_LIGHT);
    }

    @Override
    protected void show(@NotNull List<Disc> discs) {
        view.show(discs);
    }
}
