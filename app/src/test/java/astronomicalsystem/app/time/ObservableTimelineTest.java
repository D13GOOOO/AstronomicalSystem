package astronomicalsystem.app.time;

import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.simulation.Advanceable;
import astronomicalsystem.core.time.JulianDate;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ObservableTimelineTest {

    private static final JulianDate START_OF_2026 = new JulianDate(2461041.5);
    private static final TimeStep QUARTER_OF_A_DAY = new TimeStep(0.25);
    private static final TimeWarp TWO_DAYS_PER_SECOND = new TimeWarp(2.0);
    // At two days per second, one step of a quarter of a day
    private static final double ONE_STEP_IN_REAL_SECONDS = 0.125;
    private static final double HALF_A_STEP_IN_REAL_SECONDS = 0.0625;

    private final Advanceable simulation = mock();
    private final Observer<TimeFlow> observer = mock();
    private final ObservableTimeline SUT = new ObservableTimeline(simulation, START_OF_2026, QUARTER_OF_A_DAY,
            TWO_DAYS_PER_SECOND);

    static Stream<Arguments> changes() {
        return Stream.of(
                Arguments.of(Named.of("a step", (Consumer<ObservableTimeline>) timeline ->
                        timeline.elapse(ONE_STEP_IN_REAL_SECONDS))),
                Arguments.of(Named.of("speed up", (Consumer<ObservableTimeline>) ObservableTimeline::speedUp)),
                Arguments.of(Named.of("slow down", (Consumer<ObservableTimeline>) ObservableTimeline::slowDown)),
                Arguments.of(Named.of("pause", (Consumer<ObservableTimeline>) ObservableTimeline::togglePause)));
    }

    @ParameterizedTest
    @MethodSource("changes")
    void everyChangeNotifiesTheObserversWithTheNewTimeFlow(Consumer<ObservableTimeline> change) {
        SUT.addObserver(observer);

        change.accept(SUT);

        verify(observer).update(SUT, SUT.getState());
    }

    // A frame shorter than a step leaves the date where it was
    @Test
    void aFrameWithoutStepsNotifiesNobody() {
        SUT.addObserver(observer);

        SUT.elapse(HALF_A_STEP_IN_REAL_SECONDS);

        verifyNoInteractions(observer);
    }

    @Test
    void aFrameDuringAPauseNotifiesNobody() {
        SUT.togglePause();
        SUT.addObserver(observer);

        SUT.elapse(ONE_STEP_IN_REAL_SECONDS);

        verifyNoInteractions(observer);
    }
}
