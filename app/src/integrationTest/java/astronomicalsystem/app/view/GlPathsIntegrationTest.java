package astronomicalsystem.app.view;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glReadPixels;

class GlPathsIntegrationTest {

    private static final String TITLE = "Integration test";
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final float[] BLACK = {0.0f, 0.0f, 0.0f};
    private static final float[] RED = {1.0f, 0.0f, 0.0f};
    private static final Colour RED_LINE = new Colour(1.0, 0.0, 0.0);
    // One step of an 8-bit colour channel
    private static final float ONE_LEVEL = 1.0f / 255.0f;
    private static final int FORTY_PIXELS = 40;
    private static final int TWENTY_PIXELS = 20;
    // The depth a cleared image has everywhere
    private static final float FARTHEST = 1.0f;

    // Gives the OpenGL context
    private GlfwWindow window;
    private GlPaths SUT;
    private int[] viewport;

    @BeforeEach
    void openAWindow() {
        window = GlfwWindow.open(TITLE, WIDTH, HEIGHT);
        SUT = GlPaths.create();
        viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
    }

    // The buffers are deleted while the context still exists
    @AfterEach
    void closeTheWindow() {
        SUT.close();
        window.close();
    }

    // An L from the left of the centre to the centre, then up: the corner of the L is never closed
    @Test
    void aPathIsALineOfItsColourFromPointToPoint() {
        int x = viewport[2] / 2;
        int y = viewport[3] / 2;
        SUT.show(List.of(new Path(List.of(atPixel(x - FORTY_PIXELS, y), atPixel(x, y), atPixel(x, y + FORTY_PIXELS)),
                RED_LINE)));
        clearToBlack();

        SUT.draw();

        assertThat(glGetError()).isEqualTo(GL_NO_ERROR);
        assertThat(pixelAt(x - TWENTY_PIXELS, y)).containsExactly(RED, withPrecision(ONE_LEVEL));
        assertThat(pixelAt(x, y + TWENTY_PIXELS)).containsExactly(RED, withPrecision(ONE_LEVEL));
        assertThat(pixelAt(x - TWENTY_PIXELS, y + TWENTY_PIXELS)).containsExactly(BLACK, withPrecision(ONE_LEVEL));
    }

    // Two short paths on the diagonal, one each side of the centre: a line joining them would cross it
    @Test
    void separatePathsAreNotJoined() {
        int x = viewport[2] / 2;
        int y = viewport[3] / 2;
        SUT.show(List.of(
                new Path(List.of(atPixel(x - FORTY_PIXELS, y - FORTY_PIXELS), atPixel(x - TWENTY_PIXELS,
                        y - TWENTY_PIXELS)), RED_LINE),
                new Path(List.of(atPixel(x + TWENTY_PIXELS, y + TWENTY_PIXELS), atPixel(x + FORTY_PIXELS,
                        y + FORTY_PIXELS)), RED_LINE)));
        clearToBlack();

        SUT.draw();

        assertThat(pixelAt(x, y)).containsExactly(BLACK, withPrecision(ONE_LEVEL));
    }

    // The discs drawn after the paths are hidden only by nearer discs, as if the paths were not there:
    // the depth test is left on by the discs of the previous frame
    @Test
    void aPathLeavesTheDepthOfTheImageAsItWas() {
        int x = viewport[2] / 2;
        int y = viewport[3] / 2;
        SUT.show(List.of(new Path(List.of(atPixel(x - FORTY_PIXELS, y), atPixel(x + FORTY_PIXELS, y)), RED_LINE)));
        clearToBlack();
        glEnable(GL_DEPTH_TEST);

        SUT.draw();

        assertThat(depthAt(x, y)).isEqualTo(FARTHEST);
    }

    // The centre of the pixel: a line through it lights it whatever rule the driver uses for edges
    private ClipPoint atPixel(int x, int y) {
        return new ClipPoint(2.0 * (x + 0.5) / viewport[2] - 1.0, 2.0 * (y + 0.5) / viewport[3] - 1.0, 0.0, 1.0);
    }

    private static void clearToBlack() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private static float depthAt(int x, int y) {
        float[] depth = new float[1];
        glReadPixels(x, y, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, depth);
        return depth[0];
    }

    private static float[] pixelAt(int x, int y) {
        float[] pixel = new float[3];
        glReadPixels(x, y, 1, 1, GL_RGB, GL_FLOAT, pixel);
        return pixel;
    }
}
