package astronomicalsystem.app.follow;

import astronomicalsystem.app.camera.CameraTarget;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
}
