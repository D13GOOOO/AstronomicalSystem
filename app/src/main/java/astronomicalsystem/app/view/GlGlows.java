package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

/**
 * Draws glows with OpenGL: a square of two triangles around each, where the shader fades the light
 * from the middle to the edge of the inscribed circle and adds it to the picture. A glow is hidden
 * behind the bodies nearer than it, but hides nothing. Needs a current OpenGL 3.3 core context, on
 * the thread that created it.
 * <p>
 * Not covered by unit tests, since it needs a graphics card: see the integration tests.
 */
public final class GlGlows implements GlowsView, Drawable, AutoCloseable {

    // Interleaved vertex attributes, in the order of their locations in the shader: clip position
    // of the centre (4 floats), corner of the square (2), diameter (1), colour (3)
    private static final int POSITION_SIZE = 4;
    private static final int CORNER_SIZE = 2;
    private static final int DIAMETER_SIZE = 1;
    private static final int COLOUR_SIZE = 3;

    private final int program;
    private final int imageSize;
    private final Squares squares;
    // The glows of the last update, drawn again in every frame until the next one
    private List<Glow> glows = List.of();

    private GlGlows(int program, @NotNull Squares squares) {
        this.program = program;
        this.imageSize = glGetUniformLocation(program, "imageSize");
        this.squares = squares;
    }

    /**
     * @throws IllegalStateException if the shaders cannot be built
     */
    public static @NotNull GlGlows create() {
        int program = ShaderProgram.link("/shaders/glow.vert", "/shaders/glow.frag");
        return new GlGlows(program, Squares.ofAttributes(POSITION_SIZE, CORNER_SIZE, DIAMETER_SIZE, COLOUR_SIZE));
    }

    @Override
    public void show(@NotNull List<Glow> glows) {
        this.glows = List.copyOf(glows);
    }

    @Override
    public void draw() {
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        // Hidden by nearer bodies without hiding anything, the light added to what is there
        glEnable(GL_DEPTH_TEST);
        glDepthMask(false);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        glUseProgram(program);
        glUniform2f(imageSize, viewport[2], viewport[3]);
        squares.around(glows, GlGlows::vertexOf);
        glDrawArrays(GL_TRIANGLES, 0, glows.size() * Squares.VERTICES_PER_SQUARE);
        glBindVertexArray(0);
        // The other layers expect depth to be written and nothing blended
        glDisable(GL_BLEND);
        glDepthMask(true);
    }

    @Override
    public void close() {
        squares.close();
        glDeleteProgram(program);
    }



    private static float @NotNull [] vertexOf(@NotNull Glow glow, float @NotNull [] corner) {
        ClipPoint centre = glow.centre();
        Colour colour = glow.colour();
        return new float[]{(float) centre.x(), (float) centre.y(), (float) centre.z(), (float) centre.w(),
                corner[0], corner[1], (float) glow.diameter(),
                (float) colour.red(), (float) colour.green(), (float) colour.blue()};
    }
}
