package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.Colour;
import astronomicalsystem.app.view.Path;
import astronomicalsystem.app.view.PathsView;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.simulation.Trajectory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Draws the paths the bodies are going to follow.
 * <p>
 * Every point is projected, also behind the camera: OpenGL clips the lines in clip coordinates,
 * before the perspective division, so a line that passes behind the eye is cut where it leaves
 * the view and never drawn reversed.
 */
public class TrajectoriesPresenter extends ProjectingPresenter<Trajectory> {

    private final PathsView view;
    private final Map<BodyKind, Colour> colours;

    /**
     * @param colours precondition: a colour for every kind of body the simulation contains
     */
    public TrajectoriesPresenter(@NotNull PathsView view, @NotNull Projection projection,
                                 @NotNull Map<BodyKind, Colour> colours) {
        super(projection);
        this.view = view;
        this.colours = Map.copyOf(colours);
    }

    @Override
    protected void redraw() {
        view.show(latest().stream().map(this::pathOf).toList());
    }

    private @NotNull Path pathOf(@NotNull Trajectory trajectory) {
        return new Path(trajectory.points().stream().map(projection()::clipPoint).toList(),
                colours.get(trajectory.kind()));
    }
}
