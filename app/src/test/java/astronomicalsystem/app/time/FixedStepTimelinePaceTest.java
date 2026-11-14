package astronomicalsystem.app.time;

import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.simulation.Advanceable;
import astronomicalsystem.core.time.JulianDate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Steps shorter than the longest one, when the warp is slow.
 */
class FixedStepTimelinePaceTest {

    private static final JulianDate START_OF_2026 = new JulianDate(2461041.5);
    private static final TimeStep QUARTER_OF_A_DAY = new TimeStep(0.25);
    private static final int SIXTY_STEPS_PER_SECOND = 60;
    // 0.9375 d/s over sixty steps a second: a step of 1/64 of a day, shorter than a quarter
    private static final TimeWarp SLOW = new TimeWarp(0.9375);
    private static final TimeStep A_SIXTY_FOURTH_OF_A_DAY = new TimeStep(1.0 / 64.0);
    private static final double HALF_A_SECOND = 0.5;
    private static final int THIRTY_STEPS = 30;

    private final Advanceable simulation = mock();
    private final FixedStepTimeline SUT = new FixedStepTimeline(simulation, START_OF_2026, QUARTER_OF_A_DAY, SLOW,
            SIXTY_STEPS_PER_SECOND);

    @Test
    void aSlowWarpTakesShorterStepsSoThatEveryFrameMovesTheBodies() {
        SUT.elapse(HALF_A_SECOND);

        verify(simulation).advance(A_SIXTY_FOURTH_OF_A_DAY, THIRTY_STEPS);
    }

    // Thirty sixty-fourths of a day
    @Test
    void theDateMovesOnByTheShorterSteps() {
        SUT.elapse(HALF_A_SECOND);

        assertThat(SUT.timeFlow().date()).isEqualTo(new JulianDate(2461041.5 + 30.0 / 64.0));
    }
}
