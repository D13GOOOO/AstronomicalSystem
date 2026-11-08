package astronomicalsystem.app.presenter;

import astronomicalsystem.app.time.TimeFlow;
import astronomicalsystem.app.time.TimeWarp;
import astronomicalsystem.app.view.TextView;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.time.JulianDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TimeFlowPresenterTest {

    // Midnight of 1 January 2026 in UTC is 69.184 s later in TDB
    private static final JulianDate NEW_YEAR_2026 = new JulianDate(2461041.500800741);
    private static final TimeWarp TEN_DAYS_PER_SECOND = new TimeWarp(10.0);
    private static final String NEW_YEAR_2026_IN_UTC = "2026-01-01 00:00 UTC";

    private final TextView view = mock();
    private final Observable<TimeFlow> timeline = mock();
    private final TimeFlowPresenter SUT = new TimeFlowPresenter(view);

    @Test
    void showsTheDateInUtcAndTheSpeedOfSimulatedTime() {
        SUT.update(timeline, new TimeFlow(NEW_YEAR_2026, TEN_DAYS_PER_SECOND, false));

        verify(view).show(List.of(NEW_YEAR_2026_IN_UTC, "10 d/s"));
    }

    // The speed it will flow at again
    @Test
    void aPauseIsShownNextToTheSpeed() {
        SUT.update(timeline, new TimeFlow(NEW_YEAR_2026, TEN_DAYS_PER_SECOND, true));

        verify(view).show(List.of(NEW_YEAR_2026_IN_UTC, "10 d/s, paused"));
    }

    // Below a day per second the speed is given in hours per second; at most two decimals
    @ParameterizedTest
    @CsvSource(textBlock = """
            320.0,                 320 d/s
            1.25,                  1.25 d/s
            1.0,                   1 d/s
            0.625,                 15 h/s
            0.078125,              1.88 h/s
            0.041666666666666664,  1 h/s
            """)
    void theSpeedIsShownInTheLargestUnitBelowIt(double daysPerSecond, String speed) {
        SUT.update(timeline, new TimeFlow(NEW_YEAR_2026, new TimeWarp(daysPerSecond), false));

        verify(view).show(List.of(NEW_YEAR_2026_IN_UTC, speed));
    }
}
