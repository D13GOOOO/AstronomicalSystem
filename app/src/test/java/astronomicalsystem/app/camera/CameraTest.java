package astronomicalsystem.app.camera;

import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.core.math.Vector3;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CameraTest {

    private static final Vector3 ORIGIN = Vector3.ZERO;
    private static final Vector3 Y_AXIS = new Vector3(0.0, 1.0, 0.0);
    // A right angle from the bottom to the top of the image, so that the focal factor
    // 1 / tan(fov / 2) is 1; depth from 1 to 3 AU
    private static final Lens RIGHT_ANGLE_LENS = new Lens(Math.PI / 2.0, 1.0, 3.0);
    // Twice as wide as tall: the horizontal focal factor is halved
    private static final Viewport TWO_BY_ONE = new Viewport(200, 100);
    private static final double TOLERANCE = 1e-12;
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = RecursiveComparisonConfiguration.builder()
            .withComparatorForType((a, b) -> Math.abs(a - b) < TOLERANCE ? 0 : Double.compare(a, b), Double.class)
            .build();

    // With depth d along the line of sight: w = d, z = (d (far + near) - 2 far near) / (far - near),
    // x = (focal / aspect) * right offset, y = focal * up offset. The first camera stands 2 AU above
    // the origin with y up in the image; the second 2 AU behind it along -y with z up
    @ParameterizedTest
    @CsvSource(textBlock = """
            0.0,  0.0, 2.0,  0.0, 1.0, 0.0,   0.0, 0.0,  0.0,   0.0, 0.0,  1.0,  2.0
            0.0,  0.0, 2.0,  0.0, 1.0, 0.0,   0.0, 0.0,  1.0,   0.0, 0.0, -1.0,  1.0
            0.0,  0.0, 2.0,  0.0, 1.0, 0.0,   0.0, 0.0, -1.0,   0.0, 0.0,  3.0,  3.0
            0.0,  0.0, 2.0,  0.0, 1.0, 0.0,   4.0, 0.0,  0.0,   2.0, 0.0,  1.0,  2.0
            0.0,  0.0, 2.0,  0.0, 1.0, 0.0,   0.0, 2.0,  0.0,   0.0, 2.0,  1.0,  2.0
            0.0,  0.0, 2.0,  0.0, 1.0, 0.0,   0.0, 0.0,  3.0,   0.0, 0.0, -5.0, -1.0
            0.0, -2.0, 0.0,  0.0, 0.0, 1.0,   0.0, 0.0,  2.0,   0.0, 2.0,  1.0,  2.0
            0.0, -2.0, 0.0,  0.0, 0.0, 1.0,   4.0, 0.0,  0.0,   2.0, 0.0,  1.0,  2.0
            """)
    void clipPointProjectsInPerspective(double eyeX, double eyeY, double eyeZ, double upX, double upY, double upZ,
                                        double x, double y, double z,
                                        double clipX, double clipY, double clipZ, double clipW) {
        Camera SUT = new Camera(new Vector3(eyeX, eyeY, eyeZ), ORIGIN, new Vector3(upX, upY, upZ),
                RIGHT_ANGLE_LENS, TWO_BY_ONE);

        ClipPoint projected = SUT.clipPoint(new Vector3(x, y, z));

        assertThat(projected).usingRecursiveComparison(CLOSE_ENOUGH)
                .isEqualTo(new ClipPoint(clipX, clipY, clipZ, clipW));
    }

    // Half the image height (50 px) spans focal * size / depth in normalised device coordinates
    @ParameterizedTest
    @CsvSource(textBlock = """
            0.5, 2.0, 12.5
            1.0, 1.0, 50.0
            1.0, 3.0, 16.666666666666668
            """)
    void apparentSizeShrinksWithDepth(double size, double depth, double pixels) {
        Camera SUT = new Camera(new Vector3(0.0, 0.0, 2.0), ORIGIN, Y_AXIS, RIGHT_ANGLE_LENS, TWO_BY_ONE);

        double apparentSize = SUT.apparentSizeInPixels(size, new ClipPoint(0.0, 0.0, 0.0, depth));

        assertThat(apparentSize).isCloseTo(pixels, within(TOLERANCE));
    }
}
