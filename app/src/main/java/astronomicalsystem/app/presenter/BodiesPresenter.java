package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.Disc;
import astronomicalsystem.app.view.SceneView;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Shows the bodies of the simulation as discs, every time they or the camera move.
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
        return new Disc(centre, diameter, style.colour());
    }

    @Override
    protected void show(@NotNull List<Disc> discs) {
        view.show(discs);
    }
}
