package astronomicalsystem.app.time;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

// Each case starts from its own warp, next to one of the limits
class FixedStepTimelineLimitsTest {

    private static final JulianDate START_OF_2026 = new JulianDate(2461041.5);
    private static final TimeStep ONE_SIXTY_FOURTH_OF_A_DAY = new TimeStep(1.0 / 64.0);
    // One step a second at least: at the warps of these tests the step is always the longest
    private static final int ONE_STEP_PER_SECOND = 1;

    private final Advanceable simulation = mock();

    // Doubling 256 d/s would go beyond a year per second (365.25 d/s). Halving 1/2048 d/s, 42 s per
    // second, would go below half a minute per second (1/2880 d/s)
    static Stream<Arguments> changesBeyondTheLimits() {
        return Stream.of(
                Arguments.of(new TimeWarp(256.0),
                        Named.of("speed up", (Consumer<TimeControls>) TimeControls::speedUp)),
                Arguments.of(new TimeWarp(1.0 / 2048.0),
                        Named.of("slow down", (Consumer<TimeControls>) TimeControls::slowDown)));
    }

    @ParameterizedTest
    @MethodSource("changesBeyondTheLimits")
    void theWarpStaysWhereAChangeWouldTakeItBeyondItsLimits(TimeWarp nextToALimit, Consumer<TimeControls> change) {
        FixedStepTimeline SUT = new FixedStepTimeline(simulation, START_OF_2026, ONE_SIXTY_FOURTH_OF_A_DAY,
                nextToALimit, ONE_STEP_PER_SECOND);

        change.accept(SUT);

        assertThat(SUT.timeFlow().warp()).isEqualTo(nextToALimit);
    }

    // Halving 1/16 d/s, an hour and a half per second, goes below an hour per second: impacts are
    // watched in minutes
    @Test
    void theWarpGoesDownToHalfAMinutePerSecond() {
        FixedStepTimeline SUT = new FixedStepTimeline(simulation, START_OF_2026, ONE_SIXTY_FOURTH_OF_A_DAY,
                new TimeWarp(1.0 / 16.0), ONE_STEP_PER_SECOND);

        SUT.slowDown();

        assertThat(SUT.timeFlow().warp()).isEqualTo(new TimeWarp(1.0 / 32.0));
    }
}
