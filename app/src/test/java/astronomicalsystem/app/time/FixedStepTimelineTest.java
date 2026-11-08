package astronomicalsystem.app.time;

import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.simulation.Advanceable;
import astronomicalsystem.core.time.JulianDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class FixedStepTimelineTest {

    private static final JulianDate START_OF_2026 = new JulianDate(2461041.5);
    private static final JulianDate A_DAY_LATER = new JulianDate(2461042.5);
    private static final TimeStep QUARTER_OF_A_DAY = new TimeStep(0.25);
    private static final TimeWarp TWO_DAYS_PER_SECOND = new TimeWarp(2.0);
    // At two days per second, 0.0625 s of real time is 0.125 d of simulated time
    private static final double HALF_A_STEP_IN_REAL_SECONDS = 0.0625;
    // A frame after the application was blocked, for example while the window is dragged
    private static final double A_STALLED_FRAME_IN_REAL_SECONDS = 10.0;
    // One real second is two simulated days, eight steps of a quarter of a day
    private static final int STEPS_IN_ONE_REAL_SECOND = 8;
    // At two days per second, 0.25 s of real time is two steps
    private static final double TWO_STEPS_IN_REAL_SECONDS = 0.25;
    private static final double HALF_A_SECOND = 0.5;

    private final Advanceable simulation = mock();
    private final FixedStepTimeline SUT = new FixedStepTimeline(simulation, START_OF_2026, QUARTER_OF_A_DAY,
            TWO_DAYS_PER_SECOND);

    @Test
    void theTimeFlowStartsFromTheFirstDateAtTheFirstWarp() {
        TimeFlow flow = SUT.timeFlow();

        assertThat(flow).isEqualTo(new TimeFlow(START_OF_2026, TWO_DAYS_PER_SECOND, false));
    }

    // Half a second at two days per second: four steps of a quarter of a day
    @Test
    void theDateMovesOnByTheStepsTaken() {
        SUT.elapse(HALF_A_SECOND);

        assertThat(SUT.timeFlow().date()).isEqualTo(A_DAY_LATER);
    }

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

    // At four days per second, half a step of real time at two days per second is a whole step
    @Test
    void speedingUpDoublesTheWarp() {
        SUT.speedUp();
        SUT.elapse(HALF_A_STEP_IN_REAL_SECONDS);

        verify(simulation).advance(QUARTER_OF_A_DAY);
    }

    // At one day per second, the time of two steps at two days per second is only one step
    @Test
    void slowingDownHalvesTheWarp() {
        SUT.slowDown();
        SUT.elapse(TWO_STEPS_IN_REAL_SECONDS);

        verify(simulation).advance(QUARTER_OF_A_DAY);
    }

    @Test
    void aPauseStopsSimulatedTime() {
        SUT.togglePause();
        SUT.elapse(HALF_A_SECOND);

        verifyNoInteractions(simulation);
    }

    @Test
    void theSecondPauseLetsTimeFlowAgainAtTheSameWarp() {
        SUT.togglePause();
        SUT.togglePause();
        SUT.elapse(TWO_STEPS_IN_REAL_SECONDS);

        verify(simulation, times(2)).advance(QUARTER_OF_A_DAY);
    }
}
