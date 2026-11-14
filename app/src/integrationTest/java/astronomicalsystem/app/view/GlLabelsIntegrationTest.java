package astronomicalsystem.app.view;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glReadPixels;

class GlLabelsIntegrationTest {

    private static final String TITLE = "Integration test";
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    // Capitals with wide strokes: whole pixels are covered by the letters
    private static final String CAPITALS = "MMMM";
    // In the middle of the image, in pixels from its top left corner
    private static final int ANCHOR_X = 160;
    private static final int ANCHOR_Y = 120;
    // The capitals, about 14 pixels tall, lie within this band around the anchor only if they are
    // centred on it
    private static final int HALF_BAND = 10;
    private static final int TEXT_WIDTH = 100;
    private static final float HALF_BRIGHT = 0.5f;

    private GlfwWindow window;
    private GlFont font;
    private GlLabels SUT;

    @BeforeEach
    void openAWindow() {
        window = GlfwWindow.open(TITLE, WIDTH, HEIGHT);
        font = GlFont.create();
        SUT = new GlLabels(font);
    }

    @AfterEach
    void closeTheWindow() {
        font.close();
        window.close();
    }

    @Test
    void aLabelIsWrittenRightwardsFromItsAnchorAndCentredOnIt() {
        SUT.show(List.of(new Label(new ImagePoint(ANCHOR_X, ANCHOR_Y), CAPITALS)));
        clearToBlack();

        SUT.draw();

        assertThat(glGetError()).isEqualTo(GL_NO_ERROR);
        int imageHeight = viewport()[3];
        int imageWidth = viewport()[2];
        assertThat(brightestRedIn(ANCHOR_X, ANCHOR_Y - HALF_BAND, TEXT_WIDTH, 2 * HALF_BAND))
                .isGreaterThan(HALF_BRIGHT);
        assertThat(brightestRedIn(0, 0, ANCHOR_X - 1, imageHeight)).isZero();
        assertThat(brightestRedIn(0, 0, imageWidth, ANCHOR_Y - HALF_BAND)).isZero();
        assertThat(brightestRedIn(0, ANCHOR_Y + HALF_BAND, imageWidth, imageHeight - ANCHOR_Y - HALF_BAND))
                .isZero();
    }

    private static void clearToBlack() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private static int[] viewport() {
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        return viewport;
    }

    // A rectangle given from its top left corner, as the image is seen; OpenGL counts rows of pixels
    // from the bottom of the image
    private static float brightestRedIn(int left, int top, int width, int height) {
        float[] reds = new float[width * height];
        glReadPixels(left, viewport()[3] - top - height, width, height, GL_RED, GL_FLOAT, reds);
        float brightest = 0.0f;
        for (float red : reds) {
            brightest = Math.max(brightest, red);
        }
        return brightest;
    }
}
