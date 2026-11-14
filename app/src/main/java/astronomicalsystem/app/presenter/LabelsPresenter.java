package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.ImagePoint;
import astronomicalsystem.app.view.Label;
import astronomicalsystem.app.view.LabelsView;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Names the bodies of the simulation next to their discs, every time they or the camera move.
 */
public class LabelsPresenter extends ProjectedBodiesPresenter<Label> {

    // Between the edge of a disc and its label
    private static final double GAP_IN_PIXELS = 4.0;

    private final LabelsView view;

    /**
     * @param styles precondition: a style for every kind of body the simulation contains, the same
     *               the discs are drawn with
     */
    public LabelsPresenter(@NotNull LabelsView view, @NotNull Projection projection,
                           @NotNull Map<BodyKind, BodyStyle> styles) {
        super(projection, styles);
        this.view = view;
    }

    @Override
    protected @NotNull Label markOf(@NotNull Body body, @NotNull BodyStyle style, @NotNull ClipPoint centre,
                                    double diameter) {
        ImagePoint inTheImage = imagePointOf(centre);
        return new Label(new ImagePoint(inTheImage.x() + diameter / 2.0 + GAP_IN_PIXELS, inTheImage.y()),
                body.name().value());
    }

    @Override
    protected void show(@NotNull List<Label> labels) {
        view.show(labels);
    }
}
