package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL32.GL_PROGRAM_POINT_SIZE;

/**
 * Draws discs with OpenGL as round points. Needs a current OpenGL 3.3 core context, on the thread
 * that created it.
 * <p>
 * Not covered by unit tests, since it needs a graphics card: see the integration tests.
 */
public final class GlDiscs implements SceneView, Drawable, AutoCloseable {

    // Interleaved vertex attributes: clip position (4 floats), diameter (1), colour (3)
    private static final int POSITION = 0;
    private static final int DIAMETER = 1;
    private static final int COLOUR = 2;
    private static final int POSITION_SIZE = 4;
    private static final int DIAMETER_SIZE = 1;
    private static final int COLOUR_SIZE = 3;
    private static final int FLOATS_PER_DISC = POSITION_SIZE + DIAMETER_SIZE + COLOUR_SIZE;
    private static final int STRIDE = FLOATS_PER_DISC * Float.BYTES;

    private final int program;
    private final int vertexArray;
    private final int buffer;
    // The discs of the last update, drawn again in every frame until the next one
    private List<Disc> discs = List.of();

    private GlDiscs(int program, int vertexArray, int buffer) {
        this.program = program;
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
        attribute(DIAMETER, DIAMETER_SIZE, POSITION_SIZE);
        attribute(COLOUR, COLOUR_SIZE, POSITION_SIZE + DIAMETER_SIZE);
        glBindVertexArray(0);
        // Lets the vertex shader set the size of each point
        glEnable(GL_PROGRAM_POINT_SIZE);
        return new GlDiscs(program, vertexArray, buffer);
    }

    @Override
    public void show(@NotNull List<Disc> discs) {
        this.discs = List.copyOf(discs);
    }

    @Override
    public void draw() {
        glUseProgram(program);
        glBindVertexArray(vertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glBufferData(GL_ARRAY_BUFFER, vertices(), GL_STREAM_DRAW);
        glDrawArrays(GL_POINTS, 0, discs.size());
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
        float[] vertices = new float[discs.size() * FLOATS_PER_DISC];
        int index = 0;
        for (Disc disc : discs) {
            ClipPoint centre = disc.centre();
            Colour colour = disc.colour();
            float[] values = {(float) centre.x(), (float) centre.y(), (float) centre.z(), (float) centre.w(),
                    (float) disc.diameter(), (float) colour.red(), (float) colour.green(), (float) colour.blue()};
            System.arraycopy(values, 0, vertices, index, FLOATS_PER_DISC);
            index += FLOATS_PER_DISC;
        }
        return vertices;
    }
}
