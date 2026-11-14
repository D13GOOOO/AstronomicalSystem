package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.physics.Collision;
import astronomicalsystem.core.physics.CollisionResolver;
import astronomicalsystem.core.physics.Integrator;
import astronomicalsystem.core.physics.TimeStep;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulation that notifies its observers after every advance, however many steps it takes, and
 * when a body joins it; a second subject tells the collisions of every advance that has some.
 */
public class ObservableSimulation extends Simulation implements Observable<List<Body>> {

    private final List<Observer<List<Body>>> observers = new ArrayList<>();
    private final CollisionNews collisionNews = new CollisionNews();

    public ObservableSimulation(@NotNull Integrator integrator, @NotNull CollisionResolver collisions,
                                @NotNull List<Body> initialBodies) {
        super(integrator, collisions, initialBodies);
    }

    @Override
    public void advance(@NotNull TimeStep timeStep) {
        super.advance(timeStep);
        tellTheAdvance();
    }

    /**
     * Notifies the observers once, with the bodies after the last step.
     *
     * @param steps precondition: positive
     */
    @Override
    public void advance(@NotNull TimeStep timeStep, int steps) {
        super.advance(timeStep, steps);
        tellTheAdvance();
    }

    @Override
    public void add(@NotNull Body body) {
        super.add(body);
        notifyObservers();
    }

    /**
     * @return what tells its observers the collisions of every advance that has some
     */
    public @NotNull Observable<List<Collision>> collisionNews() {
        return collisionNews;
    }

    // The new bodies, and the collisions if there were any
    private void tellTheAdvance() {
        notifyObservers();
        if (!collisions().isEmpty()) {
            collisionNews.notifyObservers();
        }
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

    // A second subject: one class cannot implement Observable for two types, which have the same erasure
    private final class CollisionNews implements Observable<List<Collision>> {

        private final List<Observer<List<Collision>>> observers = new ArrayList<>();

        @Override
        public void addObserver(@NotNull Observer<List<Collision>> observer) {
            observers.add(observer);
        }

        @Override
        public void notifyObservers() {
            for (Observer<List<Collision>> observer : observers) {
                observer.update(this, getState());
            }
        }

        @Override
        public @NotNull List<Collision> getState() {
            return collisions();
        }
    }
}
