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
    // The first line of text lies within this corner of the image
    private static final int CORNER_WIDTH = 120;
    private static final int CORNER_HEIGHT = 40;
    private static final float HALF_BRIGHT = 0.5f;

    private GlfwWindow window;
    private GlText SUT;

    @BeforeEach
    void openAWindow() {
        window = GlfwWindow.open(TITLE, WIDTH, HEIGHT);
        SUT = GlText.create();
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

    private static void clearToBlack() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    // Rows of pixels are read from the bottom of the image
    private static float brightestRedInTheTopLeftCorner() {
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        float[] reds = new float[CORNER_WIDTH * CORNER_HEIGHT];
        glReadPixels(0, viewport[3] - CORNER_HEIGHT, CORNER_WIDTH, CORNER_HEIGHT, GL_RED, GL_FLOAT, reds);
        float brightest = 0.0f;
        for (float red : reds) {
            brightest = Math.max(brightest, red);
        }
        return brightest;
    }
}
