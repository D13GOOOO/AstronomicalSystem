package astronomicalsystem.app.time;

import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.simulation.Advanceable;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

// Each case starts from its own warp, next to one of the limits
class FixedStepTimelineLimitsTest {

    private static final TimeStep ONE_SIXTY_FOURTH_OF_A_DAY = new TimeStep(1.0 / 64.0);

    private final Advanceable simulation = mock();

    // Doubling 256 d/s would go beyond a year per second (365.25 d/s): one step takes 1/16384 s
    static Stream<Arguments> changesBeyondTheLimits() {
        return Stream.of(
                Arguments.of(new TimeWarp(256.0),
                        Named.of("speed up", (Consumer<TimeControls>) TimeControls::speedUp), 1.0 / 16384.0));
    }

    @ParameterizedTest
    @MethodSource("changesBeyondTheLimits")
    void theWarpStaysWhereAChangeWouldTakeItBeyondItsLimits(TimeWarp nextToALimit, Consumer<TimeControls> change,
                                                             double oneStepInRealSeconds) {
        FixedStepTimeline SUT = new FixedStepTimeline(simulation, ONE_SIXTY_FOURTH_OF_A_DAY, nextToALimit);

        change.accept(SUT);
        SUT.elapse(oneStepInRealSeconds);

        verify(simulation).advance(ONE_SIXTY_FOURTH_OF_A_DAY);
    }
}
