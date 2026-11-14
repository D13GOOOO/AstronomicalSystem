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

    // Until the timeline tells the date, which it does before the first collision
    private static final JulianDate BEFORE_THE_TIMELINE = new JulianDate(0.0);

    private final List<Observer<List<Flare>>> observers = new ArrayList<>();
    private final List<Blast> blasts = new ArrayList<>();
    private JulianDate now = BEFORE_THE_TIMELINE;
    private List<Body> bodies = List.of();
    private List<Flare> flares = List.of();

    @Override
    public void update(@NotNull Observable<List<Collision>> subject, @NotNull List<Collision> state) {
        for (Collision collision : state) {
            blasts.add(new Blast(collision, now));
        }
    }

    /**
     * @return the observer that lets the blasts unfold with simulated time
     */
    public @NotNull Observer<TimeFlow> timeObserver() {
        return (subject, flow) -> {
            now = flow.date();
            List<Flare> before = flares;
            // A blast that no longer shows anything is over
            blasts.removeIf(blast -> flaresOf(blast).isEmpty());
            flares = blasts.stream().flatMap(blast -> flaresOf(blast).stream()).toList();
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
    private @NotNull List<Flare> flaresOf(@NotNull Blast blast) {
        return bodies.stream()
                .filter(body -> body.name().equals(blast.collision().survivor().name()))
                .findFirst()
                .map(survivor -> blast.flaresAt(now, survivor.position()))
                .orElse(List.of());
    }
}
