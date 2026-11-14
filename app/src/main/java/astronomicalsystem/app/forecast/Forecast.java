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
 * Foresees the paths of the bodies of the simulation and tells its observers: at the first update
 * of the bodies, then again after a given number of updates. A prediction runs the simulation far
 * ahead, so it costs much more than an update; between two of them the paths hardly change.
 */
public class Forecast implements Observer<List<Body>>, Observable<List<Trajectory>> {

    private final Predictor predictor;
    private final int updatesPerPrediction;
    private final List<Observer<List<Trajectory>>> observers = new ArrayList<>();
    private List<Trajectory> trajectories = List.of();
    // Updates since the last prediction: none before the first one, which predicts
    private int updatesSincePrediction;

    /**
     * @param updatesPerPrediction precondition: positive
     */
    public Forecast(@NotNull Predictor predictor, int updatesPerPrediction) {
        this.predictor = predictor;
        this.updatesPerPrediction = updatesPerPrediction;
    }

    @Override
    public void update(@NotNull Observable<List<Body>> subject, @NotNull List<Body> state) {
        if (updatesSincePrediction == 0) {
            trajectories = List.copyOf(predictor.predict(state));
            notifyObservers();
        }
        updatesSincePrediction = (updatesSincePrediction + 1) % updatesPerPrediction;
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
