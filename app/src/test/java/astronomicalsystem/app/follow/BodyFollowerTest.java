package astronomicalsystem.app.follow;

import astronomicalsystem.app.camera.CameraTarget;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class BodyFollowerTest {

    private static final Vector3 ORIGIN = Vector3.ZERO;
    private static final Vector3 AT_REST = Vector3.ZERO;
    private static final Body SUN = new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), new Radius(4.65e-3),
            new Vector3(0.001, 0.002, 0.0), AT_REST);
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3e-6),
            new Radius(4.26e-5), new Vector3(1.0, 0.0, 0.0), AT_REST);
    private static final Body SUN_LATER = SUN.withMotion(new Vector3(0.003, 0.001, 0.0), AT_REST);

    private final CameraTarget camera = mock();
    private final Observable<List<Body>> simulation = mock();
    private final BodyFollower SUT = new BodyFollower(camera);

    @Test
    void theBarycentreIsFollowedAtFirst() {
        SUT.update(simulation, List.of(SUN, EARTH));

        assertThat(SUT.focus()).isEqualTo(Focus.BARYCENTRE);
        verify(camera).lookAt(ORIGIN);
    }

    // At once, without waiting for the bodies to move
    @Test
    void theFirstBodyIsFollowedAfterTheBarycentre() {
        SUT.update(simulation, List.of(SUN, EARTH));

        SUT.followNext();

        assertThat(SUT.focus()).isEqualTo(new Focus.OnBody(SUN.name()));
        verify(camera).lookAt(SUN.position());
    }

    @Test
    void theFollowedBodyIsFollowedAsItMoves() {
        SUT.update(simulation, List.of(SUN, EARTH));
        SUT.followNext();

        SUT.update(simulation, List.of(SUN_LATER, EARTH));

        verify(camera).lookAt(SUN_LATER.position());
    }

    // Merged into a heavier body, whose name the merged body keeps
    @Test
    void whenTheFollowedBodyIsGoneTheBarycentreIsFollowed() {
        SUT.update(simulation, List.of(SUN, EARTH));
        SUT.followPrevious();

        SUT.update(simulation, List.of(SUN));

        assertThat(SUT.focus()).isEqualTo(Focus.BARYCENTRE);
        verify(camera, times(2)).lookAt(ORIGIN);
    }

    // The barycentre, the Sun, the Earth and the barycentre again, in either direction
    static Stream<Arguments> choices() {
        return Stream.of(
                Arguments.of(presses("next twice", follower -> {
                    follower.followNext();
                    follower.followNext();
                }), new Focus.OnBody(EARTH.name())),
                Arguments.of(presses("next three times", follower -> {
                    follower.followNext();
                    follower.followNext();
                    follower.followNext();
                }), Focus.BARYCENTRE),
                Arguments.of(presses("next four times", follower -> {
                    follower.followNext();
                    follower.followNext();
                    follower.followNext();
                    follower.followNext();
                }), new Focus.OnBody(SUN.name())),
                Arguments.of(presses("previous once", BodyFollower::followPrevious), new Focus.OnBody(EARTH.name())),
                Arguments.of(presses("previous twice", follower -> {
                    follower.followPrevious();
                    follower.followPrevious();
                }), new Focus.OnBody(SUN.name())),
                Arguments.of(presses("previous three times", follower -> {
                    follower.followPrevious();
                    follower.followPrevious();
                    follower.followPrevious();
                }), Focus.BARYCENTRE),
                Arguments.of(presses("next then previous", follower -> {
                    follower.followNext();
                    follower.followPrevious();
                }), Focus.BARYCENTRE));
    }

    @ParameterizedTest
    @MethodSource("choices")
    void theChoicesGoRoundTheBarycentreAndTheBodiesInTheirOrder(Consumer<BodyFollower> choices, Focus focus) {
        SUT.update(simulation, List.of(SUN, EARTH));

        choices.accept(SUT);

        assertThat(SUT.focus()).isEqualTo(focus);
    }

    private static Named<Consumer<BodyFollower>> presses(String name, Consumer<BodyFollower> choices) {
        return Named.of(name, choices);
    }
}
