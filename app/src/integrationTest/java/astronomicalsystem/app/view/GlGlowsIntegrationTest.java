package astronomicalsystem.app.view;

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

class GlGlowsIntegrationTest {

    private static final String TITLE = "Integration test";
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final ClipPoint MIDDLE_OF_THE_IMAGE = new ClipPoint(0.0, 0.0, 0.0, 1.0);
    private static final double FORTY_PIXELS = 40.0;
    private static final Colour GREY = new Colour(0.4, 0.4, 0.4);
    // Two steps of an 8-bit colour channel
    private static final float TWO_LEVELS = 2.0f / 255.0f;
    // 18 px from the middle of a glow of radius 20, 0.9 of the way to its edge
    private static final int EIGHTEEN_PIXELS = 18;
    private static final float NEARLY_DARK = 0.05f;

    // Gives the OpenGL context
    private GlfwWindow window;
    private GlGlows SUT;

    @BeforeEach
    void openAWindow() {
        window = GlfwWindow.open(TITLE, WIDTH, HEIGHT);
        SUT = GlGlows.create();
    }

    // The buffers are deleted while the context still exists
    @AfterEach
    void closeTheWindow() {
        SUT.close();
        window.close();
    }

    @Test
    void aGlowHasItsColourInTheMiddleAndFadesToNothingAtItsEdge() {
        SUT.show(List.of(new Glow(MIDDLE_OF_THE_IMAGE, FORTY_PIXELS, GREY)));
        clearToBlack();

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(glGetError()).isEqualTo(GL_NO_ERROR);
        assertThat(pixelAt(centre[0], centre[1])[0]).isCloseTo(0.4f, within(TWO_LEVELS));
        assertThat(pixelAt(centre[0] + EIGHTEEN_PIXELS, centre[1])[0]).isLessThan(NEARLY_DARK);
    }

    // Light adds up: two glows of 0.4 in the same place make 0.8
    @Test
    void glowsAddTheirLight() {
        SUT.show(List.of(new Glow(MIDDLE_OF_THE_IMAGE, FORTY_PIXELS, GREY),
                new Glow(MIDDLE_OF_THE_IMAGE, FORTY_PIXELS, GREY)));
        clearToBlack();

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(pixelAt(centre[0], centre[1])[0]).isCloseTo(0.8f, within(TWO_LEVELS));
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
