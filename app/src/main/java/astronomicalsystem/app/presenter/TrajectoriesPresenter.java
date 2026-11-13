package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.Colour;
import astronomicalsystem.app.view.Path;
import astronomicalsystem.app.view.PathsView;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.simulation.Trajectory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Draws the paths the bodies are going to follow.
 * <p>
 * Every point is projected, also behind the camera: OpenGL clips the lines in clip coordinates,
 * before the perspective division, so a line that passes behind the eye is cut where it leaves
 * the view and never drawn reversed.
 */
public class TrajectoriesPresenter implements Observer<List<Trajectory>> {

    private final PathsView view;
    private final Projection projection;
    private final Map<BodyKind, Colour> colours;

    /**
     * @param colours precondition: a colour for every kind of body the simulation contains
     */
    public TrajectoriesPresenter(@NotNull PathsView view, @NotNull Projection projection,
                                 @NotNull Map<BodyKind, Colour> colours) {
        this.view = view;
        this.projection = projection;
        this.colours = Map.copyOf(colours);
    }

    @Override
    public void update(@NotNull Observable<List<Trajectory>> subject, @NotNull List<Trajectory> state) {
        view.show(state.stream().map(this::pathOf).toList());
    }

    private @NotNull Path pathOf(@NotNull Trajectory trajectory) {
        return new Path(trajectory.points().stream().map(projection::clipPoint).toList(),
                colours.get(trajectory.kind()));
    }
}
