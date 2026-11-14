package astronomicalsystem.app.impact;

import astronomicalsystem.app.camera.CurrentCamera;
import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.follow.Focus;
import astronomicalsystem.app.follow.Following;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.impact.Approach;
import astronomicalsystem.core.impact.Impactor;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.simulation.Joinable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LauncherTest {

    private static final Body SUN = new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), new Radius(4.65e-3),
            Vector3.ZERO, Vector3.ZERO);
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3.003e-6),
            new Radius(4.259e-5), new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.0172, 0.0));
    // The camera looks along the x-axis, with the y-axis to the right of the image
    private static final Vector3 RIGHTWARD = new Vector3(0.0, 1.0, 0.0);
    private static final Vector3 FORWARD = new Vector3(1.0, 0.0, 0.0);
    // From the left of the image, aiming at the side of the target that faces the camera
    private static final Approach FROM_THE_LEFT_TOWARDS_THE_EYE = new Approach(new Vector3(0.0, -1.0, 0.0),
            new Vector3(-1.0, 0.0, 0.0));

    private final Joinable system = mock();
    private final Following following = mock();
    private final CurrentCamera currentCamera = mock();
    private final Projection camera = mock();
    private Launcher SUT;

    @BeforeEach
    void createLauncherForTheSunAndTheEarth() {
        when(system.bodies()).thenReturn(List.of(SUN, EARTH));
        when(currentCamera.camera()).thenReturn(camera);
        when(camera.rightward()).thenReturn(RIGHTWARD);
        when(camera.forward()).thenReturn(FORWARD);
        SUT = new Launcher(system, following, currentCamera);
    }

    @Test
    void anImpactorIsLaunchedAtTheFollowedBody() {
        when(following.focus()).thenReturn(new Focus.OnBody(EARTH.name()));

        SUT.launch(Impactor.ASTEROID);

        verify(system).add(Impactor.ASTEROID.launchedAt(EARTH, FROM_THE_LEFT_TOWARDS_THE_EYE,
                new BodyName("Asteroid 1")));
    }

    @Test
    void nothingIsLaunchedWhileTheCameraFollowsTheBarycentre() {
        when(following.focus()).thenReturn(Focus.BARYCENTRE);

        SUT.launch(Impactor.ASTEROID);

        verify(system, never()).add(any());
    }
}
