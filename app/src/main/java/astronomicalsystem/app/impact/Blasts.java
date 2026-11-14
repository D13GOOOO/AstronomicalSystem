package astronomicalsystem.app.impact;

import astronomicalsystem.app.time.TimeFlow;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.physics.Collision;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The impacts still unfolding, and what glows of them in space: a blast for every collision, from
 * the date it was told, on the body that survived it wherever it moves.
 */
public class Blasts implements Observer<List<Collision>>, Observable<List<Flare>> {

    private final List<Observer<List<Flare>>> observers = new ArrayList<>();

    @Override
    public void update(@NotNull Observable<List<Collision>> subject, @NotNull List<Collision> state) {
    }

    /**
     * @return the observer that lets the blasts unfold with simulated time
     */
    public @NotNull Observer<TimeFlow> timeObserver() {
        return (subject, flow) -> {
        };
    }

    /**
     * @return the observer that keeps the blasts on the bodies that survived them
     */
    public @NotNull Observer<List<Body>> bodiesObserver() {
        return (subject, bodies) -> {
        };
    }

    @Override
    public void addObserver(@NotNull Observer<List<Flare>> observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer<List<Flare>> observer : observers) {
            observer.update(this, getState());
        }
    }

    @Override
    public @NotNull List<Flare> getState() {
        return List.of();
    }
}
