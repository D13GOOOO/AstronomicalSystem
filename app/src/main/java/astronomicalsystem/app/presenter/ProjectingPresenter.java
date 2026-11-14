package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Shows things of the model in space through the camera, again every time they or the camera
 * change; subclasses decide how.
 *
 * @param <E> a thing shown
 */
public abstract class ProjectingPresenter<E> implements Observer<List<E>> {

    // The latest camera and the latest things: a change of either shows them again
    private Projection projection;
    private List<E> latest = List.of();

    protected ProjectingPresenter(@NotNull Projection projection) {
        this.projection = projection;
    }

    @Override
    public final void update(@NotNull Observable<List<E>> subject, @NotNull List<E> state) {
        latest = List.copyOf(state);
        redraw();
    }

    /**
     * @return the observer that shows the things from the point of view of every new camera
     */
    public final @NotNull Observer<Projection> cameraObserver() {
        return (subject, camera) -> {
            projection = camera;
            redraw();
        };
    }

    /**
     * Shows the latest things through the latest camera.
     */
    protected abstract void redraw();

    protected final @NotNull List<E> latest() {
        return latest;
    }

    protected final @NotNull Projection projection() {
        return projection;
    }
}
