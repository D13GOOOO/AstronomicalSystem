package astronomicalsystem.app.view;

import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

/**
 * Discs wrapped in a map: its northern half is red in the west and blue in the east, its southern
 * half green.
 */
class GlDiscsTextureIntegrationTest {

    private static final String TITLE = "Integration test";
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final String HEMISPHERES = "hemispheres";
    private static final ClipPoint MIDDLE_OF_THE_IMAGE = new ClipPoint(0.0, 0.0, 0.0, 1.0);
    // The card shades pixels two by two, in pairs that start at even columns: one pixel to the right
    // of the middle of an image 320 px wide, the centre of a disc is inside a pair, not between two
    private static final ClipPoint ONE_PIXEL_RIGHT_OF_THE_MIDDLE = new ClipPoint(2.0 / WIDTH, 0.0, 0.0, 1.0);
    private static final double FORTY_PIXELS = 40.0;
    private static final Colour WHITE = new Colour(1.0, 1.0, 1.0);
    private static final Colour YELLOW = new Colour(1.0, 1.0, 0.0);
    // One step of an 8-bit colour channel
    private static final float ONE_LEVEL = 1.0f / 255.0f;
    // The prime meridian faces the viewer, the east is to the right and the north up
    private static final Vector3 TOWARDS_THE_VIEWER = new Vector3(0.0, 0.0, 1.0);
    private static final Vector3 TO_THE_RIGHT = new Vector3(1.0, 0.0, 0.0);
    private static final Vector3 UP = new Vector3(0.0, 1.0, 0.0);
    private static final Surface FACING_THE_VIEWER = new Surface.Mapped(HEMISPHERES, TOWARDS_THE_VIEWER,
            TO_THE_RIGHT, UP);
    // On a disc of radius 20: 16 px east and 6 px north of the centre is at 57 degrees east and
    // 17 degrees north; 10 px south of it at 30 degrees south
    private static final int SIXTEEN_PIXELS = 16;
    private static final int SIX_PIXELS = 6;
    private static final int TEN_PIXELS = 10;
    private static final int FOUR_COLUMNS = 4;
    private static final String RED = "red";
    private static final String GREEN = "green";
    private static final String BLUE = "blue";
    // In the order of the channels of a pixel
    private static final List<String> PRIMARIES = List.of(RED, GREEN, BLUE);
    // A star is at least 40% as bright at its edge as in the middle
    private static final float LIT = 0.35f;
    private static final float UNLIT = 0.05f;

    // Gives the OpenGL context
    private GlfwWindow window;
    private GlTextures textures;
    private GlDiscs SUT;

    @BeforeEach
    void openAWindowAndLoadTheMap() throws IOException {
        window = GlfwWindow.open(TITLE, WIDTH, HEIGHT);
        textures = GlTextures.create();
        try (InputStream map = getClass().getResourceAsStream("/textures/hemispheres.png")) {
            textures.load(HEMISPHERES, Objects.requireNonNull(map).readAllBytes());
        }
        SUT = GlDiscs.create(textures);
    }

    // The buffers and the textures are deleted while the context still exists
    @AfterEach
    void closeTheWindow() {
        SUT.close();
        textures.close();
        window.close();
    }

    @Test
    void aMappedDiscShowsItsMapWithTheEastToTheRightAndTheNorthUp() {
        SUT.show(List.of(new Disc(MIDDLE_OF_THE_IMAGE, FORTY_PIXELS, WHITE, Lighting.OWN_LIGHT, FACING_THE_VIEWER)));
        clearToBlack();

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(glGetError()).isEqualTo(GL_NO_ERROR);
        assertThat(hueAt(centre[0] + SIXTEEN_PIXELS, centre[1] + SIX_PIXELS)).isEqualTo(BLUE);
        assertThat(hueAt(centre[0] - SIXTEEN_PIXELS, centre[1] + SIX_PIXELS)).isEqualTo(RED);
        assertThat(hueAt(centre[0], centre[1] - TEN_PIXELS)).isEqualTo(GREEN);
    }

    // Four tiles by two: the northern row red, red, blue, blue from the west, the southern row green
    @Test
    void aMapInTilesIsPutTogetherRowAfterRowFromTheNorthWest() throws IOException {
        byte[] red = tile("red");
        byte[] blue = tile("blue");
        byte[] green = tile("green");
        textures.loadMosaic("tiles", FOUR_COLUMNS, List.of(red, red, blue, blue, green, green, green, green));
        Surface tiled = new Surface.Mapped("tiles", TOWARDS_THE_VIEWER, TO_THE_RIGHT, UP);
        SUT.show(List.of(new Disc(MIDDLE_OF_THE_IMAGE, FORTY_PIXELS, WHITE, Lighting.OWN_LIGHT, tiled)));
        clearToBlack();

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(glGetError()).isEqualTo(GL_NO_ERROR);
        assertThat(hueAt(centre[0] + SIXTEEN_PIXELS, centre[1] + SIX_PIXELS)).isEqualTo(BLUE);
        assertThat(hueAt(centre[0] - SIXTEEN_PIXELS, centre[1] + SIX_PIXELS)).isEqualTo(RED);
        assertThat(hueAt(centre[0], centre[1] - TEN_PIXELS)).isEqualTo(GREEN);
    }

    private byte[] tile(String colour) throws IOException {
        try (InputStream file = getClass().getResourceAsStream("/textures/" + colour + ".png")) {
            return Objects.requireNonNull(file).readAllBytes();
        }
    }

    // The view was never given a map with that name
    @Test
    void aDiscWhoseMapIsMissingIsDrawnInItsColour() {
        Surface unknown = new Surface.Mapped("unknown", TOWARDS_THE_VIEWER, TO_THE_RIGHT, UP);
        SUT.show(List.of(new Disc(MIDDLE_OF_THE_IMAGE, FORTY_PIXELS, YELLOW, Lighting.OWN_LIGHT, unknown)));
        clearToBlack();

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(pixelAt(centre[0], centre[1]))
                .containsExactly(new float[]{1.0f, 1.0f, 0.0f}, withPrecision(ONE_LEVEL));
    }

    @Test
    void aFileThatIsNotAnImageIsRefused() {
        byte[] notAnImage = {1, 2, 3, 4};

        assertThatThrownBy(() -> textures.load("broken", notAnImage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Map broken cannot be read");
    }

    // 180 degrees of longitude face the viewer: there the right edge of the map meets the left one,
    // blue in the east and red in the west. A pixel on that line, far enough north of the equator
    // (17 degrees and more), must be of those colours: the green of the south would come from a copy
    // of the map so small that it is all one colour
    @Test
    void theEdgesOfTheMapMeetWithoutAShow() {
        Surface seenFromBehind = new Surface.Mapped(HEMISPHERES, new Vector3(0.0, 0.0, -1.0),
                new Vector3(-1.0, 0.0, 0.0), UP);
        SUT.show(List.of(new Disc(ONE_PIXEL_RIGHT_OF_THE_MIDDLE, FORTY_PIXELS, WHITE, Lighting.OWN_LIGHT,
                seenFromBehind)));
        clearToBlack();

        SUT.draw();

        int[] centre = centreOfTheImage();
        assertThat(greenAlongTheLine(centre[0] + 1, centre[1] + SIX_PIXELS, centre[1] + SIXTEEN_PIXELS))
                .isLessThan(UNLIT);
    }

    // The most green light among the pixels of the column x and its two neighbours, between the rows
    private static float greenAlongTheLine(int x, int fromY, int toY) {
        float most = 0.0f;
        for (int column = x - 1; column <= x + 1; column++) {
            for (int row = fromY; row <= toY; row++) {
                most = Math.max(most, pixelAt(column, row)[1]);
            }
        }
        return most;
    }

    // The only primary colour the pixel has light of; "mixed" if it has more than one, "dark" if none
    private static String hueAt(int x, int y) {
        float[] pixel = pixelAt(x, y);
        List<String> lit = new ArrayList<>();
        for (int channel = 0; channel < PRIMARIES.size(); channel++) {
            if (pixel[channel] > UNLIT) {
                lit.add(pixel[channel] > LIT ? PRIMARIES.get(channel) : "dim");
            }
        }
        return switch (lit.size()) {
            case 0 -> "dark";
            case 1 -> lit.getFirst();
            default -> "mixed";
        };
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
