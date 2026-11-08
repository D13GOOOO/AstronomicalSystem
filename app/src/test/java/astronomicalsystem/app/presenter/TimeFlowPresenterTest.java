package astronomicalsystem.app.presenter;

import astronomicalsystem.app.time.TimeFlow;
import astronomicalsystem.app.time.TimeWarp;
import astronomicalsystem.app.view.TextView;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.time.JulianDate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TimeFlowPresenterTest {

    // Midnight of 1 January 2026 in UTC is 69.184 s later in TDB
    private static final JulianDate NEW_YEAR_2026 = new JulianDate(2461041.500800741);
    private static final TimeWarp TEN_DAYS_PER_SECOND = new TimeWarp(10.0);

    private final TextView view = mock();
    private final Observable<TimeFlow> timeline = mock();
    private final TimeFlowPresenter SUT = new TimeFlowPresenter(view);

    @Test
    void showsTheDateInUtcAndTheSpeedOfSimulatedTime() {
        SUT.update(timeline, new TimeFlow(NEW_YEAR_2026, TEN_DAYS_PER_SECOND, false));

        verify(view).show(List.of("2026-01-01 00:00 UTC", "10 d/s"));
    }
}
