package astronomicalsystem.app.forecast;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.simulation.Predictor;
import astronomicalsystem.core.simulation.Trajectory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Foresees the paths of the bodies of the simulation and tells its observers.
 */
public class Forecast implements Observer<List<Body>>, Observable<List<Trajectory>> {

    public Forecast(@NotNull Predictor predictor) {
    }

    @Override
    public void update(@NotNull Observable<List<Body>> subject, @NotNull List<Body> state) {
    }

    @Override
    public void addObserver(@NotNull Observer<List<Trajectory>> observer) {
    }

    @Override
    public void notifyObservers() {
    }

    @Override
    public @NotNull List<Trajectory> getState() {
        return List.of();
    }
}
