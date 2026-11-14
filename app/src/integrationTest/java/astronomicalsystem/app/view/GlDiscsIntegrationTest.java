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
    private static final double FORTY_PIXELS = 40.0;
    private static final int FIFTEEN_PIXELS = 15;
    private static final int FIVE_PIXELS = 5;
    private static final ClipPoint BEYOND_THE_RIGHT_EDGE = new ClipPoint(1.2, 0.0, 0.0, 1.0);
    private static final double A_HUNDRED_AND_TWENTY_PIXELS = 120.0;
    private static final double FOUR_HUNDRED_PIXELS = 400.0;
    private static final int A_HUNDRED_AND_FIFTY_PIXELS = 150;
    private static final Colour WHITE = new Colour(1.0, 1.0, 1.0);
    private static final Lighting FROM_THE_RIGHT = new Lighting.LitFrom(1.0, 0.0, 0.0);
    private static final float BRIGHT = 0.7f;
    private static final float DARK = 0.1f;
    private static final float NEARLY_WHITE = 0.98f;
    private static final int EIGHTEEN_PIXELS = 18;
    // From the centre of the disc, 8.5 px right and 8.5 px up: 12 px away, outside the circle of
    // radius 10 but inside the square that encloses it
    private static final int DIAGONAL_OFFSET = 8;

    // Gives the OpenGL context
    private GlfwWindow window;
    private GlTextures textures;
    private GlDiscs SUT;

    @BeforeEach
    void openAWindow() {
        window = GlfwWindow.open(TITLE, WIDTH, HEIGHT);
        textures = GlTextures.create();
        SUT = GlDiscs.create(textures);
    }

    // The buffers are deleted while the context still exists
    @AfterEach
    void closeTheWindow() {
        SUT.close();
        textures.close();
        window.close();
    }

    @Test
    void aDiscIsDrawnAsARoundSpotOfItsColour() {
        SUT.show(List.of(new Disc(MIDDLE_OF_THE_IMAGE, TWENTY_PIXELS, new Colour(1.0, 0.0, 0.0), Lighting.OWN_LIGHT,
                Surface.PLAIN)));
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
        SUT.show(List.of(new Disc(NEARER, TWENTY_PIXELS, new Colour(1.0, 0.0, 0.0), Lighting.OWN_LIGHT, Surface.PLAIN),
                new Disc(FARTHER, TWENTY_PIXELS, new Colour(0.0, 0.0, 1.0), Lighting.OWN_LIGHT, Surface.PLAIN)));
        clearToBlack();

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(pixelAt(centre[0], centre[1])).containsExactly(RED, withPrecision(ONE_LEVEL));
    }

    // Lit from the right: 15 px right of the centre the surface faces the light at about 41 degrees,
    // 15 px left it faces away from it, and only the faint light of the sky reaches it
    @Test
    void aLitDiscIsBrightOnTheSideOfTheLightAndDarkOnTheOther() {
        SUT.show(List.of(new Disc(MIDDLE_OF_THE_IMAGE, FORTY_PIXELS, WHITE, FROM_THE_RIGHT, Surface.PLAIN)));
        clearToBlack();

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(pixelAt(centre[0] + FIFTEEN_PIXELS, centre[1])[0]).isGreaterThan(BRIGHT);
        assertThat(pixelAt(centre[0] - FIFTEEN_PIXELS, centre[1])[0]).isLessThan(DARK);
    }

    // The centre a tenth of the width beyond the right edge, the radius much more: the left part of
    // the disc is in the image, close to its right edge
    @Test
    void aDiscWhoseCentreIsOutsideTheImageStillShowsThePartInside() {
        SUT.show(List.of(new Disc(BEYOND_THE_RIGHT_EDGE, A_HUNDRED_AND_TWENTY_PIXELS, new Colour(1.0, 0.0, 0.0),
                Lighting.OWN_LIGHT, Surface.PLAIN)));
        clearToBlack();

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(pixelAt(2 * centre[0] - FIVE_PIXELS, centre[1])[0]).isGreaterThan(BRIGHT);
    }

    // Points are drawn at most as large as the card allows, 255 px on some: a body seen from close by
    // is larger, and 150 px from its centre it must still be drawn
    @Test
    void aDiscLargerThanTheLargestPointIsDrawnWhole() {
        SUT.show(List.of(new Disc(MIDDLE_OF_THE_IMAGE, FOUR_HUNDRED_PIXELS, new Colour(1.0, 0.0, 0.0),
                Lighting.OWN_LIGHT, Surface.PLAIN)));
        clearToBlack();

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(pixelAt(centre[0] + A_HUNDRED_AND_FIFTY_PIXELS, centre[1])[0]).isGreaterThan(BRIGHT);
    }

    // The light of a star comes from deeper and hotter layers in the middle: I = 1 - u (1 - mu), with
    // mu the cosine between the normal and the line of sight and u = 0.6, as for the Sun. 18 px from
    // the centre of a disc of radius 20, mu = 0.44 and I = 0.66
    @Test
    void aDiscWithItsOwnLightIsDarkerAtTheEdge() {
        SUT.show(List.of(new Disc(MIDDLE_OF_THE_IMAGE, FORTY_PIXELS, WHITE, Lighting.OWN_LIGHT, Surface.PLAIN)));
        clearToBlack();

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(pixelAt(centre[0], centre[1])[0]).isGreaterThan(NEARLY_WHITE);
        assertThat(pixelAt(centre[0] + EIGHTEEN_PIXELS, centre[1])[0]).isBetween(0.62f, 0.70f);
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
