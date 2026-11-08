package astronomicalsystem.app.time;

import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.simulation.Advanceable;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Timeline that notifies its observers every time its time flow changes.
 */
public class ObservableTimeline extends FixedStepTimeline implements Observable<TimeFlow> {

    private final List<Observer<TimeFlow>> observers = new ArrayList<>();

    public ObservableTimeline(@NotNull Advanceable simulation, @NotNull JulianDate start, @NotNull TimeStep step,
                              @NotNull TimeWarp warp) {
        super(simulation, start, step, warp);
    }

    @Override
    public void elapse(double realSeconds) {
        TimeFlow before = timeFlow();
        super.elapse(realSeconds);
        notifyObserversIfChangedFrom(before);
    }

    @Override
    public void speedUp() {
        TimeFlow before = timeFlow();
        super.speedUp();
        notifyObserversIfChangedFrom(before);
    }

    @Override
    public void slowDown() {
        TimeFlow before = timeFlow();
        super.slowDown();
        notifyObserversIfChangedFrom(before);
    }

    @Override
    public void togglePause() {
        TimeFlow before = timeFlow();
        super.togglePause();
        notifyObserversIfChangedFrom(before);
    }

    @Override
    public void addObserver(@NotNull Observer<TimeFlow> observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer<TimeFlow> observer : observers) {
            observer.update(this, getState());
        }
    }

    @Override
    public @NotNull TimeFlow getState() {
        return timeFlow();
    }

    private void notifyObserversIfChangedFrom(@NotNull TimeFlow before) {
        if (!timeFlow().equals(before)) {
            notifyObservers();
        }
    }
}
