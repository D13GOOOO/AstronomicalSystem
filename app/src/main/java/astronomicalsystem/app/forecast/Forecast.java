package astronomicalsystem.app.forecast;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.simulation.Predictor;
import astronomicalsystem.core.simulation.Trajectory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Foresees the paths of the bodies of the simulation and tells its observers.
 */
public class Forecast implements Observer<List<Body>>, Observable<List<Trajectory>> {

    private final Predictor predictor;
    private final List<Observer<List<Trajectory>>> observers = new ArrayList<>();
    private List<Trajectory> trajectories = List.of();

    public Forecast(@NotNull Predictor predictor) {
        this.predictor = predictor;
    }

    @Override
    public void update(@NotNull Observable<List<Body>> subject, @NotNull List<Body> state) {
        trajectories = List.copyOf(predictor.predict(state));
        notifyObservers();
    }

    @Override
    public void addObserver(@NotNull Observer<List<Trajectory>> observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer<List<Trajectory>> observer : observers) {
            observer.update(this, getState());
        }
    }

    @Override
    public @NotNull List<Trajectory> getState() {
        return trajectories;
    }
}
