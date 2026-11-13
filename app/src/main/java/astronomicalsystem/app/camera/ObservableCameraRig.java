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
        Projection before = camera();
        super.orbitBy(azimuthAngle, elevationAngle);
        notifyObserversIfChangedFrom(before);
    }

    @Override
    public void zoomBy(double factor) {
        Projection before = camera();
        super.zoomBy(factor);
        notifyObserversIfChangedFrom(before);
    }

    @Override
    public void resize(@NotNull Viewport newViewport) {
        Projection before = camera();
        super.resize(newViewport);
        notifyObserversIfChangedFrom(before);
    }

    @Override
    public void lookAt(@NotNull Vector3 newTarget) {
        Projection before = camera();
        super.lookAt(newTarget);
        notifyObserversIfChangedFrom(before);
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

    // The rig replaces its camera only when it changes, so a different object means a new picture
    private void notifyObserversIfChangedFrom(@NotNull Projection before) {
        if (camera() != before) {
            notifyObservers();
        }
    }
}
