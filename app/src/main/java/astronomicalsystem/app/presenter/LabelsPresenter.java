package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.ImagePoint;
import astronomicalsystem.app.view.Label;
import astronomicalsystem.app.view.LabelsView;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Names the bodies of the simulation next to their discs.
 */
public class LabelsPresenter implements Observer<List<Body>> {

    // Between the edge of a disc and its label
    private static final double GAP_IN_PIXELS = 4.0;

    private final LabelsView view;
    private final Projection projection;
    private final Map<BodyKind, BodyStyle> styles;

    /**
     * @param styles precondition: a style for every kind of body the simulation contains, the same
     *               the discs are drawn with
     */
    public LabelsPresenter(@NotNull LabelsView view, @NotNull Projection projection,
                           @NotNull Map<BodyKind, BodyStyle> styles) {
        this.view = view;
        this.projection = projection;
        this.styles = Map.copyOf(styles);
    }

    @Override
    public void update(@NotNull Observable<List<Body>> subject, @NotNull List<Body> state) {
        List<Label> labels = new ArrayList<>();
        for (Body body : state) {
            labels.add(labelOf(body, projection.clipPoint(body.position())));
        }
        view.show(labels);
    }

    private @NotNull Label labelOf(@NotNull Body body, @NotNull ClipPoint centre) {
        double apparentDiameter = projection.apparentSizeInPixels(2.0 * body.radius().astronomicalUnits(), centre);
        double diameter = Math.max(styles.get(body.kind()).minimumDiameter(), apparentDiameter);
        ImagePoint inTheImage = projection.imagePoint(centre);
        return new Label(new ImagePoint(inTheImage.x() + diameter / 2.0 + GAP_IN_PIXELS, inTheImage.y()),
                body.name().value());
    }
}
