package astronomicalsystem.app.view;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;
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

class GlDiscsIntegrationTest {

    private static final String TITLE = "Integration test";
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final float[] BLACK = {0.0f, 0.0f, 0.0f};
    private static final float[] RED = {1.0f, 0.0f, 0.0f};
    // One step of an 8-bit colour channel
    private static final float ONE_LEVEL = 1.0f / 255.0f;
    private static final ClipPoint MIDDLE_OF_THE_IMAGE = new ClipPoint(0.0, 0.0, 0.0, 1.0);
    // In the middle of the image, a quarter of the depth range in front of and behind the centre
    private static final ClipPoint NEARER = new ClipPoint(0.0, 0.0, -0.5, 1.0);
    private static final ClipPoint FARTHER = new ClipPoint(0.0, 0.0, 0.5, 1.0);
    private static final double TWENTY_PIXELS = 20.0;
    // From the centre of the disc, 8.5 px right and 8.5 px up: 12 px away, outside the circle of
    // radius 10 but inside the square that encloses it
    private static final int DIAGONAL_OFFSET = 8;

    // Gives the OpenGL context
    private GlfwWindow window;
    private GlDiscs SUT;

    @BeforeEach
    void openAWindow() {
        window = GlfwWindow.open(TITLE, WIDTH, HEIGHT);
        SUT = GlDiscs.create();
    }

    // The buffers are deleted while the context still exists
    @AfterEach
    void closeTheWindow() {
        SUT.close();
        window.close();
    }

    @Test
    void aDiscIsDrawnAsARoundSpotOfItsColour() {
        SUT.show(List.of(new Disc(MIDDLE_OF_THE_IMAGE, TWENTY_PIXELS, new Colour(1.0, 0.0, 0.0))));
        clearToBlack();

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(glGetError()).isEqualTo(GL_NO_ERROR);
        assertThat(pixelAt(centre[0], centre[1])).containsExactly(RED, withPrecision(ONE_LEVEL));
        assertThat(pixelAt(centre[0] + DIAGONAL_OFFSET, centre[1] + DIAGONAL_OFFSET))
                .containsExactly(BLACK, withPrecision(ONE_LEVEL));
    }

    @Test
    void aNearerDiscHidesAFartherOneDrawnAfterIt() {
        SUT.show(List.of(new Disc(NEARER, TWENTY_PIXELS, new Colour(1.0, 0.0, 0.0)),
                new Disc(FARTHER, TWENTY_PIXELS, new Colour(0.0, 0.0, 1.0))));
        clearToBlack();

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(pixelAt(centre[0], centre[1])).containsExactly(RED, withPrecision(ONE_LEVEL));
    }

    private static void clearToBlack() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
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
