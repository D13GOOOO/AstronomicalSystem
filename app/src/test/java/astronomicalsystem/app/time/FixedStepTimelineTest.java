package astronomicalsystem.app.time;

import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.simulation.Advanceable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FixedStepTimelineTest {

    private static final TimeStep QUARTER_OF_A_DAY = new TimeStep(0.25);
    private static final TimeWarp TWO_DAYS_PER_SECOND = new TimeWarp(2.0);
    // At two days per second, 0.0625 s of real time is 0.125 d of simulated time
    private static final double HALF_A_STEP_IN_REAL_SECONDS = 0.0625;
    // A frame after the application was blocked, for example while the window is dragged
    private static final double A_STALLED_FRAME_IN_REAL_SECONDS = 10.0;
    // One real second is two simulated days, eight steps of a quarter of a day
    private static final int STEPS_IN_ONE_REAL_SECOND = 8;

    private final Advanceable simulation = mock();
    private final FixedStepTimeline SUT = new FixedStepTimeline(simulation, QUARTER_OF_A_DAY, TWO_DAYS_PER_SECOND);

    @ParameterizedTest
    @CsvSource(textBlock = """
            0.0625, 0
            0.125,  1
            0.3125, 2
            0.5,    4
            """)
    void advancesByTheWholeStepsInTheWarpedTime(double realSeconds, int steps) {
        SUT.elapse(realSeconds);

        verify(simulation, times(steps)).advance(QUARTER_OF_A_DAY);
    }

    @Test
    void theRemainderOfAFrameCarriesOverToTheNext() {
        SUT.elapse(HALF_A_STEP_IN_REAL_SECONDS);
        SUT.elapse(HALF_A_STEP_IN_REAL_SECONDS);

        verify(simulation).advance(QUARTER_OF_A_DAY);
    }

    @Test
    void aStalledFrameCountsAsOneSecondAtMost() {
        SUT.elapse(A_STALLED_FRAME_IN_REAL_SECONDS);

        verify(simulation, times(STEPS_IN_ONE_REAL_SECOND)).advance(QUARTER_OF_A_DAY);
    }
}
