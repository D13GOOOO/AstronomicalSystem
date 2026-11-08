package astronomicalsystem.app.time;

import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.simulation.Advanceable;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

/**
 * Timeline that notifies its observers every time its time flow changes.
 */
public class ObservableTimeline extends FixedStepTimeline implements Observable<TimeFlow> {

    public ObservableTimeline(@NotNull Advanceable simulation, @NotNull JulianDate start, @NotNull TimeStep step,
                              @NotNull TimeWarp warp) {
        super(simulation, start, step, warp);
    }

    @Override
    public void addObserver(@NotNull Observer<TimeFlow> observer) {
    }

    @Override
    public void notifyObservers() {
    }

    @Override
    public @NotNull TimeFlow getState() {
        return timeFlow();
    }
}
