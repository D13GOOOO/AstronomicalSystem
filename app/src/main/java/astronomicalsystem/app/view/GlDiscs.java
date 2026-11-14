package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
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
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
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
    // 0 if it has its own light)
    private static final int POSITION = 0;
    private static final int CORNER = 1;
    private static final int DIAMETER = 2;
    private static final int COLOUR = 3;
    private static final int LIGHT = 4;
    private static final int POSITION_SIZE = 4;
    private static final int CORNER_SIZE = 2;
    private static final int DIAMETER_SIZE = 1;
    private static final int COLOUR_SIZE = 3;
    private static final int LIGHT_SIZE = 4;
    private static final int FLOATS_PER_VERTEX =
            POSITION_SIZE + CORNER_SIZE + DIAMETER_SIZE + COLOUR_SIZE + LIGHT_SIZE;
    private static final int STRIDE = FLOATS_PER_VERTEX * Float.BYTES;
    // Two triangles cover the square from (-1, -1) to (1, 1)
    private static final float[][] CORNERS = {{-1.0f, -1.0f}, {1.0f, -1.0f}, {1.0f, 1.0f},
            {-1.0f, -1.0f}, {1.0f, 1.0f}, {-1.0f, 1.0f}};

    private final int program;
    private final int imageSize;
    private final int vertexArray;
    private final int buffer;
    // The discs of the last update, drawn again in every frame until the next one
    private List<Disc> discs = List.of();

    private GlDiscs(int program, int vertexArray, int buffer) {
        this.program = program;
        this.imageSize = glGetUniformLocation(program, "imageSize");
        this.vertexArray = vertexArray;
        this.buffer = buffer;
    }

    /**
     * @throws IllegalStateException if the shaders cannot be built
     */
    public static @NotNull GlDiscs create() {
        int program = ShaderProgram.link("/shaders/disc.vert", "/shaders/disc.frag");
        int vertexArray = glGenVertexArrays();
        int buffer = glGenBuffers();
        glBindVertexArray(vertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        attribute(POSITION, POSITION_SIZE, 0);
        attribute(CORNER, CORNER_SIZE, POSITION_SIZE);
        attribute(DIAMETER, DIAMETER_SIZE, POSITION_SIZE + CORNER_SIZE);
        attribute(COLOUR, COLOUR_SIZE, POSITION_SIZE + CORNER_SIZE + DIAMETER_SIZE);
        attribute(LIGHT, LIGHT_SIZE, POSITION_SIZE + CORNER_SIZE + DIAMETER_SIZE + COLOUR_SIZE);
        glBindVertexArray(0);
        return new GlDiscs(program, vertexArray, buffer);
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
        glBufferData(GL_ARRAY_BUFFER, vertices(), GL_STREAM_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, discs.size() * CORNERS.length);
        glBindVertexArray(0);
    }

    @Override
    public void close() {
        glDeleteBuffers(buffer);
        glDeleteVertexArrays(vertexArray);
        glDeleteProgram(program);
    }

    private static void attribute(int location, int size, int offsetInFloats) {
        glVertexAttribPointer(location, size, GL_FLOAT, false, STRIDE, (long) offsetInFloats * Float.BYTES);
        glEnableVertexAttribArray(location);
    }

    private float @NotNull [] vertices() {
        float[] vertices = new float[discs.size() * CORNERS.length * FLOATS_PER_VERTEX];
        int index = 0;
        for (Disc disc : discs) {
            for (float[] corner : CORNERS) {
                System.arraycopy(vertexOf(disc, corner), 0, vertices, index, FLOATS_PER_VERTEX);
                index += FLOATS_PER_VERTEX;
            }
        }
        return vertices;
    }

    private static float @NotNull [] vertexOf(@NotNull Disc disc, float @NotNull [] corner) {
        ClipPoint centre = disc.centre();
        Colour colour = disc.colour();
        float[] light = lightOf(disc.lighting());
        return new float[]{(float) centre.x(), (float) centre.y(), (float) centre.z(), (float) centre.w(),
                corner[0], corner[1], (float) disc.diameter(),
                (float) colour.red(), (float) colour.green(), (float) colour.blue(),
                light[0], light[1], light[2], light[3]};
    }

    private static float @NotNull [] lightOf(@NotNull Lighting lighting) {
        return switch (lighting) {
            case Lighting.OwnLight ownLight -> new float[]{0.0f, 0.0f, 0.0f, 0.0f};
            case Lighting.LitFrom(double x, double y, double z) -> new float[]{(float) x, (float) y, (float) z, 1.0f};
        };
    }
}
