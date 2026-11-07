package astronomicalsystem.app.camera;

import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.core.math.Vector3;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CameraRigTest {

    private static final double TOLERANCE = 1e-12;
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = RecursiveComparisonConfiguration.builder()
            .withComparatorForType((a, b) -> Math.abs(a - b) < TOLERANCE ? 0 : Double.compare(a, b), Double.class)
            .build();

    private static final Vector3 TARGET = new Vector3(1.0, 2.0, 3.0);
    // 1 AU above the target, along the ecliptic north
    private static final Vector3 NORTH_OF_THE_TARGET = TARGET.plus(new Vector3(0.0, 0.0, 1.0));
    // 1 AU from the target along the positive x-axis
    private static final Vector3 EAST_OF_THE_TARGET = TARGET.plus(new Vector3(1.0, 0.0, 0.0));
    private static final double QUARTER_TURN = Math.PI / 2.0;
    private static final double HALF_THE_DISTANCE = 0.5;
    // 2 AU away on the ecliptic, looking along the positive y-axis
    private static final Orbit ON_THE_ECLIPTIC = new Orbit(2.0, 0.0, 0.0);
    // Focal factor 1, depth from 1 to 3 AU: the target, 2 AU away, has z = w / 2
    private static final Lens RIGHT_ANGLE_LENS = new Lens(Math.PI / 2.0, 1.0, 3.0);
    private static final Viewport TWO_BY_ONE = new Viewport(200, 100);
    private static final Viewport SQUARE = new Viewport(100, 100);

    private final CameraRig SUT = new CameraRig(TARGET, ON_THE_ECLIPTIC, RIGHT_ANGLE_LENS, TWO_BY_ONE);

    @Test
    void theCameraLooksAtTheTargetWithTheEclipticNorthUp() {
        Projection camera = SUT.camera();

        assertThat(camera.clipPoint(TARGET)).usingRecursiveComparison(CLOSE_ENOUGH)
                .isEqualTo(new ClipPoint(0.0, 0.0, 1.0, 2.0));
        assertThat(camera.clipPoint(NORTH_OF_THE_TARGET)).usingRecursiveComparison(CLOSE_ENOUGH)
                .isEqualTo(new ClipPoint(0.0, 1.0, 1.0, 2.0));
    }

    // A quarter turn brings the camera onto the positive x-axis: the point east of the target is
    // then straight ahead, 1 AU away, on the near plane
    @Test
    void orbitingMovesTheCameraAroundTheTarget() {
        SUT.orbitBy(QUARTER_TURN, 0.0);

        assertThat(SUT.camera().clipPoint(EAST_OF_THE_TARGET)).usingRecursiveComparison(CLOSE_ENOUGH)
                .isEqualTo(new ClipPoint(0.0, 0.0, -1.0, 1.0));
    }

    // Halfway to the target, which is then on the near plane
    @Test
    void zoomingMovesTheCameraTowardsTheTarget() {
        SUT.zoomBy(HALF_THE_DISTANCE);

        assertThat(SUT.camera().clipPoint(TARGET)).usingRecursiveComparison(CLOSE_ENOUGH)
                .isEqualTo(new ClipPoint(0.0, 0.0, -1.0, 1.0));
    }

    // On a square image the horizontal focal factor is no longer halved by the aspect ratio
    @Test
    void resizingFitsTheImageToTheNewViewport() {
        SUT.resize(SQUARE);

        assertThat(SUT.camera().clipPoint(EAST_OF_THE_TARGET)).usingRecursiveComparison(CLOSE_ENOUGH)
                .isEqualTo(new ClipPoint(1.0, 0.0, 1.0, 2.0));
    }
}
