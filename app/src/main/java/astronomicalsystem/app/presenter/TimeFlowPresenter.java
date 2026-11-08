package astronomicalsystem.app.presenter;

import astronomicalsystem.app.time.TimeFlow;
import astronomicalsystem.app.view.TextView;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

/**
 * Tells the user the date of the simulation and how fast simulated time flows.
 */
public class TimeFlowPresenter implements Observer<TimeFlow> {

    public TimeFlowPresenter(@NotNull TextView view) {
    }

    @Override
    public void update(@NotNull Observable<TimeFlow> subject, @NotNull TimeFlow state) {
    }
}
