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

class GlTextIntegrationTest {

    private static final String TITLE = "Integration test";
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    // Capitals with wide strokes: whole pixels of the first line are covered by the letters
    private static final String CAPITALS = "MMMM";
    // A line of text lies within this corner of the image
    private static final int CORNER_WIDTH = 120;
    private static final int CORNER_HEIGHT = 40;
    // Half of the margin that keeps the text away from the edges
    private static final int MARGIN_STRIP_WIDTH = 5;
    private static final float HALF_BRIGHT = 0.5f;

    private GlfwWindow window;
    private GlText SUT;

    @BeforeEach
    void openAWindow() {
        window = GlfwWindow.open(TITLE, WIDTH, HEIGHT);
        SUT = GlText.create(Corner.TOP_LEFT);
    }

    // The font atlas is deleted while the context still exists
    @AfterEach
    void closeTheWindow() {
        SUT.close();
        window.close();
    }

    @Test
    void aLineOfTextLightsTheTopLeftCornerOfTheImage() {
        SUT.show(List.of(CAPITALS));
        clearToBlack();

        SUT.draw();

        assertThat(glGetError()).isEqualTo(GL_NO_ERROR);
        assertThat(brightestRedInTheTopLeftCorner()).isGreaterThan(HALF_BRIGHT);
    }

    @Test
    void noLinesLeaveTheImageDark() {
        SUT.show(List.of());
        clearToBlack();

        SUT.draw();

        assertThat(brightestRedInTheTopLeftCorner()).isZero();
    }

    @Test
    void textForTheBottomLeftCornerLightsThatCornerOnly() {
        try (GlText bottomLeft = GlText.create(Corner.BOTTOM_LEFT)) {
            bottomLeft.show(List.of(CAPITALS));
            clearToBlack();

            bottomLeft.draw();

            assertThat(brightestRedInTheBottomLeftCorner()).isGreaterThan(HALF_BRIGHT);
            assertThat(brightestRedInTheTopLeftCorner()).isZero();
        }
    }

    // Right-aligned: within the image and short of its right edge
    @Test
    void textForTheBottomRightCornerEndsAMarginAwayFromTheRightEdge() {
        try (GlText bottomRight = GlText.create(Corner.BOTTOM_RIGHT)) {
            bottomRight.show(List.of(CAPITALS));
            clearToBlack();

            bottomRight.draw();

            assertThat(brightestRedInTheBottomRightCorner()).isGreaterThan(HALF_BRIGHT);
            assertThat(brightestRedInTheRightMargin()).isZero();
            assertThat(brightestRedInTheBottomLeftCorner()).isZero();
        }
    }

    private static void clearToBlack() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private static float brightestRedInTheTopLeftCorner() {
        return brightestRedIn(0, viewport()[3] - CORNER_HEIGHT, CORNER_WIDTH, CORNER_HEIGHT);
    }

    private static float brightestRedInTheBottomLeftCorner() {
        return brightestRedIn(0, 0, CORNER_WIDTH, CORNER_HEIGHT);
    }

    private static float brightestRedInTheBottomRightCorner() {
        return brightestRedIn(viewport()[2] - CORNER_WIDTH, 0, CORNER_WIDTH, CORNER_HEIGHT);
    }

    private static float brightestRedInTheRightMargin() {
        return brightestRedIn(viewport()[2] - MARGIN_STRIP_WIDTH, 0, MARGIN_STRIP_WIDTH, CORNER_HEIGHT);
    }

    private static int[] viewport() {
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        return viewport;
    }

    // Rows of pixels are counted from the bottom of the image
    private static float brightestRedIn(int firstColumn, int lowestRow, int width, int height) {
        float[] reds = new float[width * height];
        glReadPixels(firstColumn, lowestRow, width, height, GL_RED, GL_FLOAT, reds);
        float brightest = 0.0f;
        for (float red : reds) {
            brightest = Math.max(brightest, red);
        }
        return brightest;
    }
}
