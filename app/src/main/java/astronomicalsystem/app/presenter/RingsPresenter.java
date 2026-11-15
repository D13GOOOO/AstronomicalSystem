package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.Colour;
import astronomicalsystem.app.view.RingSystem;
import astronomicalsystem.app.view.RingsView;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.rings.Rings;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Shows the rings of the bodies that have them, lit by the brightest star, every time the bodies or
 * the camera move.
 */
public class RingsPresenter extends ProjectedBodiesPresenter<RingSystem> {

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
    protected @NotNull RingSystem markOf(@NotNull Body body, @NotNull BodyStyle style, @NotNull ClipPoint centre,
                                         double diameter) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void show(@NotNull List<RingSystem> ringSystems) {
        view.show(ringSystems);
    }
}
