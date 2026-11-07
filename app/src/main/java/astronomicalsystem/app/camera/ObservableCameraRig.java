package astronomicalsystem.app.camera;

import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Camera rig that notifies its observers every time its camera changes.
 */
public class ObservableCameraRig extends CameraRig implements Observable<Projection> {

    private final List<Observer<Projection>> observers = new ArrayList<>();

    public ObservableCameraRig(@NotNull Vector3 target, @NotNull Orbit orbit, @NotNull Lens lens,
                               @NotNull Viewport viewport) {
        super(target, orbit, lens, viewport);
    }

    @Override
    public void orbitBy(double azimuthAngle, double elevationAngle) {
        super.orbitBy(azimuthAngle, elevationAngle);
        notifyObservers();
    }

    @Override
    public void zoomBy(double factor) {
        super.zoomBy(factor);
        notifyObservers();
    }

    @Override
    public void resize(@NotNull Viewport newViewport) {
        super.resize(newViewport);
        notifyObservers();
    }

    @Override
    public void addObserver(@NotNull Observer<Projection> observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer<Projection> observer : observers) {
            observer.update(this, getState());
        }
    }

    @Override
    public @NotNull Projection getState() {
        return camera();
    }
}
