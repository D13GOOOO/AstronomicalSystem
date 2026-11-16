package astronomicalsystem.app.view;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glScissor;

class GlScreenshotIntegrationTest {

    private static final String TITLE = "Integration test";
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    // 8-bit levels of the colours the frame is cleared to: 0.2, 0.4 and 0.6 of 255
    private static final int[] BLUE = {51, 102, 153};
    private static final int[] RED = {153, 51, 51};

    @TempDir
    private Path folder;
    // Gives the OpenGL context
    private GlfwWindow window;
    private GlScreenshot SUT;

    @BeforeEach
    void openAWindow() {
        window = GlfwWindow.open(TITLE, WIDTH, HEIGHT);
        SUT = new GlScreenshot();
    }

    @AfterEach
    void closeTheWindow() {
        window.close();
    }

    // OpenGL counts rows from the bottom of the image, a picture from the top
    @Test
    void theFrameIsWrittenToTheFileUpright() throws IOException {
        Path file = folder.resolve("frame.png");
        SUT.capture(file);
        drawRedOverBlue();

        SUT.draw();

        assertThat(file).isRegularFile();
        BufferedImage picture = ImageIO.read(file.toFile());
        int[] image = sizeOfTheImage();
        assertThat(picture.getWidth()).isEqualTo(image[0]);
        assertThat(picture.getHeight()).isEqualTo(image[1]);
        assertThat(colourAt(picture, 0, 0)).containsExactly(RED);
        assertThat(colourAt(picture, 0, image[1] - 1)).containsExactly(BLUE);
    }

    @Test
    void aCaptureIsWrittenWithOneFrameOnly() throws IOException {
        Path file = folder.resolve("frame.png");
        SUT.capture(file);
        drawRedOverBlue();
        SUT.draw();
        Files.delete(file);

        SUT.draw();

        assertThat(file).doesNotExist();
    }

    // The pictures folder does not exist before the first screenshot
    @Test
    void theFolderOfThePictureIsCreated() {
        Path file = folder.resolve("pictures").resolve("frame.png");
        SUT.capture(file);
        drawRedOverBlue();

        SUT.draw();

        assertThat(file).isRegularFile();
    }

    // Blue below, red in the upper half
    private static void drawRedOverBlue() {
        int[] image = sizeOfTheImage();
        glClearColor(0.2f, 0.4f, 0.6f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        glEnable(GL_SCISSOR_TEST);
        glScissor(0, image[1] / 2, image[0], image[1] - image[1] / 2);
        glClearColor(0.6f, 0.2f, 0.2f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_SCISSOR_TEST);
    }

    private static int[] sizeOfTheImage() {
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        return new int[]{viewport[2], viewport[3]};
    }

    private static int[] colourAt(BufferedImage picture, int x, int y) {
        int rgb = picture.getRGB(x, y);
        return new int[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF};
    }
}
