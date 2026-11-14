package astronomicalsystem.app.impact;

import astronomicalsystem.app.follow.Focus;
import astronomicalsystem.app.follow.Following;
import astronomicalsystem.app.time.TimeControls;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.physics.Collision;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SlowMotionTest {

    private static final BodyName EARTH = new BodyName("Earth");
    private static final Vector3 UP = new Vector3(0.0, 0.0, 1.0);
    private static final Collision EARTH_HIT = new Collision(new Body(EARTH, BodyKind.PLANET, new Mass(3.003e-6),
            new Radius(4.259e-5), Vector3.ZERO, Vector3.ZERO), UP, UP, 0.01, 1e-9);

    private final Following following = mock();
    private final TimeControls time = mock();
    private final Observable<List<Collision>> simulation = mock();
    private final SlowMotion SUT = new SlowMotion(following, time);

    @Test
    void anImpactOnTheFollowedBodySlowsTimeAsMuchAsItCan() {
        when(following.focus()).thenReturn(new Focus.OnBody(EARTH));

        SUT.update(simulation, List.of(EARTH_HIT));

        verify(time).slowest();
    }

    // The camera shows the barycentre, or another body
    static Stream<Focus> elsewhere() {
        return Stream.of(Focus.BARYCENTRE, new Focus.OnBody(new BodyName("Mars")));
    }

    @ParameterizedTest
    @MethodSource("elsewhere")
    void anImpactTheUserIsNotWatchingLeavesTimeAlone(Focus focus) {
        when(following.focus()).thenReturn(focus);

        SUT.update(simulation, List.of(EARTH_HIT));

        verifyNoInteractions(time);
    }
}
