package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.physics.Integrator;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Simulation that notifies its observers every time its bodies change.
 */
public class ObservableSimulation extends Simulation implements Observable<List<Body>> {

    public ObservableSimulation(@NotNull Integrator integrator, @NotNull List<Body> initialBodies) {
        super(integrator, initialBodies);
    }

    @Override
    public void addObserver(@NotNull Observer<List<Body>> observer) {
    }

    @Override
    public void notifyObservers() {
    }

    @Override
    public @NotNull List<Body> getState() {
        return List.of();
    }
}
