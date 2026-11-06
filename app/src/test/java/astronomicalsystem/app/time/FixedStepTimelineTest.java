package astronomicalsystem.app.time;

import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.simulation.Advanceable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FixedStepTimelineTest {

    private static final TimeStep QUARTER_OF_A_DAY = new TimeStep(0.25);
    private static final TimeWarp TWO_DAYS_PER_SECOND = new TimeWarp(2.0);

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
}
