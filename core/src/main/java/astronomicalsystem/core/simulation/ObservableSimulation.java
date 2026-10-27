package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.physics.Integrator;
import astronomicalsystem.core.physics.TimeStep;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulation that notifies its observers every time its bodies change.
 */
public class ObservableSimulation extends Simulation implements Observable<List<Body>> {

    private final List<Observer<List<Body>>> observers = new ArrayList<>();

    public ObservableSimulation(@NotNull Integrator integrator, @NotNull List<Body> initialBodies) {
        super(integrator, initialBodies);
    }

    @Override
    public void advance(@NotNull TimeStep timeStep) {
        super.advance(timeStep);
        notifyObservers();
    }

    @Override
    public void addObserver(@NotNull Observer<List<Body>> observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer<List<Body>> observer : observers) {
            observer.update(this, getState());
        }
    }

    @Override
    public @NotNull List<Body> getState() {
        return bodies();
    }
}
