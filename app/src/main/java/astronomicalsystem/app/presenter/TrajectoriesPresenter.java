package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.Colour;
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
 */
public class TrajectoriesPresenter implements Observer<List<Trajectory>> {

    /**
     * @param colours precondition: a colour for every kind of body the simulation contains
     */
    public TrajectoriesPresenter(@NotNull PathsView view, @NotNull Projection projection,
                                 @NotNull Map<BodyKind, Colour> colours) {
    }

    @Override
    public void update(@NotNull Observable<List<Trajectory>> subject, @NotNull List<Trajectory> state) {
    }
}
