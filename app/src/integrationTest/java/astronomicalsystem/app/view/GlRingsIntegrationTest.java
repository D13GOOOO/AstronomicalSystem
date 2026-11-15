package astronomicalsystem.app.view;

import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glReadPixels;

class GlRingsIntegrationTest {

    private static final String TITLE = "Integration test";
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final ClipPoint MIDDLE_OF_THE_IMAGE = new ClipPoint(0.0, 0.0, 0.0, 1.0);
    // A sphere 20 pixels in radius, with a band from two to three radii: from 40 to 60 pixels
    private static final double FORTY_PIXELS = 40.0;
    private static final RingSystem.Band BAND = new RingSystem.Band(2.0, 3.0, new Colour(0.6, 0.3, 0.15), 0.5);
    private static final int INSIDE_THE_BAND = 30;
    private static final int ON_THE_BAND = 50;
    private static final int BEYOND_THE_BAND = 70;
    private static final Vector3 TOWARDS_THE_VIEWER = new Vector3(0.0, 0.0, 1.0);
    // Rings seen nearly edge-on, their axis up in the image and a quarter towards the viewer: four
    // times narrower up and down, the band crosses the sphere from 10 to 15 pixels above and below
    // its centre, in front of it below and behind it above
    private static final Vector3 NEARLY_ACROSS_THE_LINE_OF_SIGHT = new Vector3(0.0, Math.sqrt(0.9375), 0.25);
    private static final int ACROSS_THE_SPHERE = 12;
    // Light from the right of the image: the shadow of the sphere, as wide as the sphere, falls on
    // the rings to its left
    private static final Vector3 TOWARDS_THE_RIGHT = new Vector3(1.0, 0.0, 0.0);
    // 40 pixels to the left and 30 up: on the band, 50 pixels from the centre, and above the shadow
    private static final int LEFT_OF_THE_SPHERE = 40;
    private static final int ABOVE_THE_SHADOW = 30;
    // Two steps of an 8-bit colour channel
    private static final float TWO_LEVELS = 2.0f / 255.0f;

    // Gives the OpenGL context
    private GlfwWindow window;
    private GlRings SUT;

    @BeforeEach
    void openAWindow() {
        window = GlfwWindow.open(TITLE, WIDTH, HEIGHT);
        SUT = GlRings.create();
    }

    // The buffers are deleted while the context still exists
    @AfterEach
    void closeTheWindow() {
        SUT.close();
        window.close();
    }

    @Test
    void aBandSeenFaceOnShowsItsLightBetweenItsEdgesAndNothingElsewhere() {
        SUT.show(List.of(new RingSystem(MIDDLE_OF_THE_IMAGE, FORTY_PIXELS, TOWARDS_THE_VIEWER, TOWARDS_THE_VIEWER,
                List.of(BAND))));
        clearTo(0.0f);

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(glGetError()).isEqualTo(GL_NO_ERROR);
        assertThat(pixelAt(centre[0] + ON_THE_BAND, centre[1])).containsExactly(new float[]{0.6f, 0.3f, 0.15f},
                within(TWO_LEVELS));
        assertThat(pixelAt(centre[0] + INSIDE_THE_BAND, centre[1])).containsOnly(0.0f);
        assertThat(pixelAt(centre[0] + BEYOND_THE_BAND, centre[1])).containsOnly(0.0f);
    }

    // Over white the band adds its light to the half of the white that it lets through
    @Test
    void aBandHidesAsMuchOfWhatIsBehindItAsItsOpacity() {
        SUT.show(List.of(new RingSystem(MIDDLE_OF_THE_IMAGE, FORTY_PIXELS, TOWARDS_THE_VIEWER, TOWARDS_THE_VIEWER,
                List.of(BAND))));
        clearTo(1.0f);

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(pixelAt(centre[0] + ON_THE_BAND, centre[1])).containsExactly(new float[]{1.0f, 0.8f, 0.65f},
                within(TWO_LEVELS));
        assertThat(pixelAt(centre[0] + BEYOND_THE_BAND, centre[1])).containsOnly(1.0f);
    }

    @Test
    void theSphereHidesTheRingsThatPassBehindIt() {
        SUT.show(List.of(new RingSystem(MIDDLE_OF_THE_IMAGE, FORTY_PIXELS, NEARLY_ACROSS_THE_LINE_OF_SIGHT,
                TOWARDS_THE_VIEWER, List.of(BAND))));
        clearTo(0.0f);

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(pixelAt(centre[0], centre[1] - ACROSS_THE_SPHERE)).containsExactly(new float[]{0.6f, 0.3f, 0.15f},
                within(TWO_LEVELS));
        assertThat(pixelAt(centre[0], centre[1] + ACROSS_THE_SPHERE)).containsOnly(0.0f);
    }

    @Test
    void theRingsAreDarkInTheShadowOfTheSphere() {
        SUT.show(List.of(new RingSystem(MIDDLE_OF_THE_IMAGE, FORTY_PIXELS, TOWARDS_THE_VIEWER, TOWARDS_THE_RIGHT,
                List.of(BAND))));
        clearTo(0.0f);

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(pixelAt(centre[0] - ON_THE_BAND, centre[1])).containsOnly(0.0f);
        assertThat(pixelAt(centre[0] + ON_THE_BAND, centre[1])).containsExactly(new float[]{0.6f, 0.3f, 0.15f},
                within(TWO_LEVELS));
        assertThat(pixelAt(centre[0] - LEFT_OF_THE_SPHERE, centre[1] + ABOVE_THE_SHADOW))
                .containsExactly(new float[]{0.6f, 0.3f, 0.15f}, within(TWO_LEVELS));
    }

    private static void clearTo(float grey) {
        glClearColor(grey, grey, grey, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private static int[] centreOfTheImage() {
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        return new int[]{viewport[2] / 2, viewport[3] / 2};
    }

    private static float[] pixelAt(int x, int y) {
        float[] pixel = new float[3];
        glReadPixels(x, y, 1, 1, GL_RGB, GL_FLOAT, pixel);
        return pixel;
    }
}
