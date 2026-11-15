package astronomicalsystem.app.view;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform2fv;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

/**
 * Draws rings with OpenGL around the spheres of the discs: a square of two triangles around each
 * system of rings, where the shader finds the band that every pixel sees. The rings hide in part
 * what is behind them, and are hidden by the bodies nearer than their centre. Of a system with more
 * than eight bands, the first eight are drawn. Needs a current OpenGL 3.3 core context, on the
 * thread that created it.
 * <p>
 * Not covered by unit tests, since it needs a graphics card: see the integration tests.
 */
public final class GlRings implements RingsView, Drawable, AutoCloseable {

    // Interleaved vertex attributes, in the order of their locations in the shader: clip position
    // of the centre (4 floats), corner of the square (2), diameter of the sphere (1)
    private static final int POSITION_SIZE = 4;
    private static final int CORNER_SIZE = 2;
    private static final int DIAMETER_SIZE = 1;
    // As many as the arrays of the shader hold
    private static final int MOST_BANDS = 8;
    private static final int FLOATS_PER_EDGES = 2;
    private static final int FLOATS_PER_LIGHT = 4;

    private final int program;
    private final int imageSize;
    private final int extent;
    private final int axis;
    private final int towardsTheLight;
    private final int bandCount;
    private final int edges;
    private final int lights;
    private final Squares squares;
    // The rings of the last update, drawn again in every frame until the next one
    private List<RingSystem> rings = List.of();

    private GlRings(int program, @NotNull Squares squares) {
        this.program = program;
        this.imageSize = glGetUniformLocation(program, "imageSize");
        this.extent = glGetUniformLocation(program, "extent");
        this.axis = glGetUniformLocation(program, "axis");
        this.towardsTheLight = glGetUniformLocation(program, "towardsTheLight");
        this.bandCount = glGetUniformLocation(program, "bandCount");
        this.edges = glGetUniformLocation(program, "edges");
        this.lights = glGetUniformLocation(program, "lights");
        this.squares = squares;
    }

    /**
     * @throws IllegalStateException if the shaders cannot be built
     */
    public static @NotNull GlRings create() {
        int program = ShaderProgram.link("/shaders/ring.vert", "/shaders/ring.frag");
        return new GlRings(program, Squares.ofAttributes(POSITION_SIZE, CORNER_SIZE, DIAMETER_SIZE));
    }

    @Override
    public void show(@NotNull List<RingSystem> rings) {
        this.rings = List.copyOf(rings);
    }

    @Override
    public void draw() {
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        // Hidden by nearer bodies but not by their own sphere, which is as deep as they are; the
        // rings hide nothing wholly, so they leave no depth, and their light is laid over what is
        // behind them, dimmed by as much as they hide
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glDepthMask(false);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glUseProgram(program);
        glUniform2f(imageSize, viewport[2], viewport[3]);
        squares.around(rings, GlRings::vertexOf);
        // One by one: each system has its own bands
        for (int index = 0; index < rings.size(); index++) {
            describe(rings.get(index));
            glDrawArrays(GL_TRIANGLES, index * Squares.VERTICES_PER_SQUARE, Squares.VERTICES_PER_SQUARE);
        }
        glBindVertexArray(0);
        // The other layers expect depth to be written, nearer things only to pass, and nothing blended
        glDisable(GL_BLEND);
        glDepthMask(true);
        glDepthFunc(GL_LESS);
    }

    @Override
    public void close() {
        squares.close();
        glDeleteProgram(program);
    }

    // Tells the shader about the system that is drawn next
    private void describe(@NotNull RingSystem system) {
        List<RingSystem.Band> bands = system.bands().subList(0, Math.min(MOST_BANDS, system.bands().size()));
        float[] edgesOfBands = new float[MOST_BANDS * FLOATS_PER_EDGES];
        float[] lightsOfBands = new float[MOST_BANDS * FLOATS_PER_LIGHT];
        double outermost = 0.0;
        for (int index = 0; index < bands.size(); index++) {
            RingSystem.Band band = bands.get(index);
            Colour light = band.light();
            edgesOfBands[index * FLOATS_PER_EDGES] = (float) band.inner();
            edgesOfBands[index * FLOATS_PER_EDGES + 1] = (float) band.outer();
            lightsOfBands[index * FLOATS_PER_LIGHT] = (float) light.red();
            lightsOfBands[index * FLOATS_PER_LIGHT + 1] = (float) light.green();
            lightsOfBands[index * FLOATS_PER_LIGHT + 2] = (float) light.blue();
            lightsOfBands[index * FLOATS_PER_LIGHT + 3] = (float) band.opacity();
            outermost = Math.max(outermost, band.outer());
        }
        Vector3 perpendicular = system.axis();
        Vector3 light = system.towardsTheLight();
        glUniform1f(extent, (float) outermost);
        glUniform3f(axis, (float) perpendicular.x(), (float) perpendicular.y(), (float) perpendicular.z());
        glUniform3f(towardsTheLight, (float) light.x(), (float) light.y(), (float) light.z());
        glUniform1i(bandCount, bands.size());
        glUniform2fv(edges, edgesOfBands);
        glUniform4fv(lights, lightsOfBands);
    }

    private static float @NotNull [] vertexOf(@NotNull RingSystem system, float @NotNull [] corner) {
        ClipPoint centre = system.centre();
        return new float[]{(float) centre.x(), (float) centre.y(), (float) centre.z(), (float) centre.w(),
                corner[0], corner[1], (float) system.diameter()};
    }
}
