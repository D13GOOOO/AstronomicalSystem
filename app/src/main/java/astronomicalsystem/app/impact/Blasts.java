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
 * the first date told after it, on the body that survived it wherever it moves.
 */
public class Blasts implements Observer<List<Collision>>, Observable<List<Flare>> {

    private final List<Observer<List<Flare>>> observers = new ArrayList<>();
    private final List<Blast> blasts = new ArrayList<>();
    // Told while the timeline advances the simulation, before the date of the frame: they start
    // from that date, which may be hours after the steps of the frame that went before
    private final List<Collision> untold = new ArrayList<>();
    private List<Body> bodies = List.of();
    private List<Flare> flares = List.of();

    @Override
    public void update(@NotNull Observable<List<Collision>> subject, @NotNull List<Collision> state) {
        untold.addAll(state);
    }

    /**
     * @return the observer that lets the blasts unfold with simulated time
     */
    public @NotNull Observer<TimeFlow> timeObserver() {
        return (subject, flow) -> {
            JulianDate now = flow.date();
            untold.forEach(collision -> blasts.add(new Blast(collision, now)));
            untold.clear();
            List<Flare> before = flares;
            // A blast that no longer shows anything is over
            blasts.removeIf(blast -> flaresOf(blast, now).isEmpty());
            flares = blasts.stream().flatMap(blast -> flaresOf(blast, now).stream()).toList();
            // Nothing to tell while nothing glows, before or now
            if (!before.isEmpty() || !flares.isEmpty()) {
                notifyObservers();
            }
        };
    }

    /**
     * @return the observer that keeps the blasts on the bodies that survived them
     */
    public @NotNull Observer<List<Body>> bodiesObserver() {
        return (subject, state) -> bodies = List.copyOf(state);
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
        return flares;
    }

    // Where the body that survived the blast is now
    private @NotNull List<Flare> flaresOf(@NotNull Blast blast, @NotNull JulianDate now) {
        return bodies.stream()
                .filter(body -> body.name().equals(blast.collision().survivor().name()))
                .findFirst()
                .map(survivor -> blast.flaresAt(now, survivor.position()))
                .orElse(List.of());
    }
}
