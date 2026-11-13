package astronomicalsystem.app.follow;

import astronomicalsystem.app.camera.CameraTarget;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ObservableBodyFollowerTest {

    private static final Vector3 AT_REST = Vector3.ZERO;
    private static final Body SUN = new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), new Radius(4.65e-3),
            Vector3.ZERO, AT_REST);
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3e-6),
            new Radius(4.26e-5), new Vector3(1.0, 0.0, 0.0), AT_REST);
    private static final Body EARTH_LATER = EARTH.withMotion(new Vector3(0.0, 1.0, 0.0), AT_REST);
    // Shared with the rows of the parameterized test; never verified
    private static final Observable<List<Body>> SIMULATION = mock();

    private final CameraTarget camera = mock();
    private final Observer<Focus> observer = mock();
    private final ObservableBodyFollower SUT = new ObservableBodyFollower(camera);

    // Following the Earth, the last of the two bodies
    @BeforeEach
    void followTheEarth() {
        SUT.update(SIMULATION, List.of(SUN, EARTH));
        SUT.followPrevious();
        SUT.addObserver(observer);
    }

    static Stream<Arguments> changes() {
        return Stream.of(
                Arguments.of(Named.of("next", (Consumer<ObservableBodyFollower>) BodyFollower::followNext)),
                Arguments.of(Named.of("previous", (Consumer<ObservableBodyFollower>) BodyFollower::followPrevious)),
                Arguments.of(Named.of("the Earth merged into the Sun",
                        (Consumer<ObservableBodyFollower>) follower -> follower.update(SIMULATION, List.of(SUN)))));
    }

    @ParameterizedTest
    @MethodSource("changes")
    void everyChangeOfFocusNotifiesTheObservers(Consumer<ObservableBodyFollower> change) {
        change.accept(SUT);

        verify(observer).update(SUT, SUT.getState());
    }

    @Test
    void theFollowedBodyMovingNotifiesNobody() {
        SUT.update(SIMULATION, List.of(SUN, EARTH_LATER));

        verifyNoInteractions(observer);
    }
}
