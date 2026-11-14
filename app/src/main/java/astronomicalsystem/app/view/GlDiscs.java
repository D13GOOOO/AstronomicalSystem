package astronomicalsystem.app.view;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Draws discs with OpenGL as spheres seen from afar: a square of two triangles around each, where
 * the shader keeps the inscribed circle and lights it. Squares, unlike points, have no largest size
 * and are drawn also when their centre is out of the image. Needs a current OpenGL 3.3 core
 * context, on the thread that created it.
 * <p>
 * Not covered by unit tests, since it needs a graphics card: see the integration tests.
 */
public final class GlDiscs implements SceneView, Drawable, AutoCloseable {

    // Interleaved vertex attributes: clip position of the centre (4 floats), corner of the square
    // (2), diameter (1), colour (3), light (4: the direction towards it, then 1 if the disc is lit,
    // 0 if it has its own light), the three axes of the body (3 each; zero for a plain disc)
    private static final int POSITION = 0;
    private static final int CORNER = 1;
    private static final int DIAMETER = 2;
    private static final int COLOUR = 3;
    private static final int LIGHT = 4;
    private static final int BODY_X = 5;
    private static final int BODY_Y = 6;
    private static final int BODY_Z = 7;
    private static final int POSITION_SIZE = 4;
    private static final int CORNER_SIZE = 2;
    private static final int DIAMETER_SIZE = 1;
    private static final int COLOUR_SIZE = 3;
    private static final int LIGHT_SIZE = 4;
    private static final int AXIS_SIZE = 3;
    private static final int AXES_OFFSET = POSITION_SIZE + CORNER_SIZE + DIAMETER_SIZE + COLOUR_SIZE + LIGHT_SIZE;
    private static final int FLOATS_PER_VERTEX = AXES_OFFSET + 3 * AXIS_SIZE;
    private static final float[] NO_AXES = new float[3 * AXIS_SIZE];

    private final int program;
    private final int imageSize;
    private final int mapped;
    private final int vertexArray;
    private final int buffer;
    private final GlTextures textures;
    // The discs of the last update, drawn again in every frame until the next one
    private List<Disc> discs = List.of();

    private GlDiscs(int program, int vertexArray, int buffer, @NotNull GlTextures textures) {
        this.textures = textures;
        this.program = program;
        this.imageSize = glGetUniformLocation(program, "imageSize");
        this.mapped = glGetUniformLocation(program, "mapped");
        this.vertexArray = vertexArray;
        this.buffer = buffer;
    }

    /**
     * @param textures where the maps of the mapped discs are looked for; it must outlive the discs
     * @throws IllegalStateException if the shaders cannot be built
     */
    public static @NotNull GlDiscs create(@NotNull GlTextures textures) {
        int program = ShaderProgram.link("/shaders/disc.vert", "/shaders/disc.frag");
        int vertexArray = glGenVertexArrays();
        int buffer = glGenBuffers();
        glBindVertexArray(vertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        Squares.attribute(POSITION, POSITION_SIZE, 0, FLOATS_PER_VERTEX);
        Squares.attribute(CORNER, CORNER_SIZE, POSITION_SIZE, FLOATS_PER_VERTEX);
        Squares.attribute(DIAMETER, DIAMETER_SIZE, POSITION_SIZE + CORNER_SIZE, FLOATS_PER_VERTEX);
        Squares.attribute(COLOUR, COLOUR_SIZE, POSITION_SIZE + CORNER_SIZE + DIAMETER_SIZE, FLOATS_PER_VERTEX);
        Squares.attribute(LIGHT, LIGHT_SIZE, POSITION_SIZE + CORNER_SIZE + DIAMETER_SIZE + COLOUR_SIZE,
                FLOATS_PER_VERTEX);
        Squares.attribute(BODY_X, AXIS_SIZE, AXES_OFFSET, FLOATS_PER_VERTEX);
        Squares.attribute(BODY_Y, AXIS_SIZE, AXES_OFFSET + AXIS_SIZE, FLOATS_PER_VERTEX);
        Squares.attribute(BODY_Z, AXIS_SIZE, AXES_OFFSET + 2 * AXIS_SIZE, FLOATS_PER_VERTEX);
        glBindVertexArray(0);
        return new GlDiscs(program, vertexArray, buffer, textures);
    }

    @Override
    public void show(@NotNull List<Disc> discs) {
        this.discs = List.copyOf(discs);
    }

    @Override
    public void draw() {
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        // Nearer discs hide farther ones, whatever the order they are drawn in; set at every frame,
        // since other layers may draw without depth
        glEnable(GL_DEPTH_TEST);
        glUseProgram(program);
        glUniform2f(imageSize, viewport[2], viewport[3]);
        glBindVertexArray(vertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glBufferData(GL_ARRAY_BUFFER, Squares.vertices(discs, FLOATS_PER_VERTEX, GlDiscs::vertexOf),
                GL_STREAM_DRAW);
        // One by one: each mapped disc samples its own texture
        for (int index = 0; index < discs.size(); index++) {
            glUniform1i(mapped, isMapped(discs.get(index).surface()) ? 1 : 0);
            glDrawArrays(GL_TRIANGLES, index * Squares.VERTICES_PER_SQUARE, Squares.VERTICES_PER_SQUARE);
        }
        glBindVertexArray(0);
    }

    @Override
    public void close() {
        glDeleteBuffers(buffer);
        glDeleteVertexArrays(vertexArray);
        glDeleteProgram(program);
    }



    private static float @NotNull [] vertexOf(@NotNull Disc disc, float @NotNull [] corner) {
        ClipPoint centre = disc.centre();
        Colour colour = disc.colour();
        float[] light = lightOf(disc.lighting());
        float[] axes = axesOf(disc.surface());
        return new float[]{(float) centre.x(), (float) centre.y(), (float) centre.z(), (float) centre.w(),
                corner[0], corner[1], (float) disc.diameter(),
                (float) colour.red(), (float) colour.green(), (float) colour.blue(),
                light[0], light[1], light[2], light[3],
                axes[0], axes[1], axes[2], axes[3], axes[4], axes[5], axes[6], axes[7], axes[8]};
    }

    // Binds the map of a mapped disc, if the textures have it: without it the disc is drawn plain
    private boolean isMapped(@NotNull Surface surface) {
        return switch (surface) {
            case Surface.Plain plain -> false;
            case Surface.Mapped map -> textures.bind(map.texture());
        };
    }

    private static float @NotNull [] axesOf(@NotNull Surface surface) {
        return switch (surface) {
            case Surface.Plain plain -> NO_AXES;
            case Surface.Mapped(String texture, Vector3 x, Vector3 y, Vector3 z) -> new float[]{
                    (float) x.x(), (float) x.y(), (float) x.z(), (float) y.x(), (float) y.y(), (float) y.z(),
                    (float) z.x(), (float) z.y(), (float) z.z()};
        };
    }

    private static float @NotNull [] lightOf(@NotNull Lighting lighting) {
        return switch (lighting) {
            case Lighting.OwnLight ownLight -> new float[]{0.0f, 0.0f, 0.0f, 0.0f};
            case Lighting.LitFrom(double x, double y, double z) -> new float[]{(float) x, (float) y, (float) z, 1.0f};
        };
    }
}
