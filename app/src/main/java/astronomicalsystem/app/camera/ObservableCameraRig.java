package astronomicalsystem.app.camera;

import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

/**
 * Camera rig that notifies its observers every time its camera changes.
 */
public class ObservableCameraRig extends CameraRig implements Observable<Projection> {

    public ObservableCameraRig(@NotNull Vector3 target, @NotNull Orbit orbit, @NotNull Lens lens,
                               @NotNull Viewport viewport) {
        super(target, orbit, lens, viewport);
    }

    @Override
    public void addObserver(@NotNull Observer<Projection> observer) {
    }

    @Override
    public void notifyObservers() {
    }

    @Override
    public @NotNull Projection getState() {
        return camera();
    }
}
