package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
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

/**
 * Draws paths with OpenGL as line strips. Needs a current OpenGL 3.3 core context, on the thread
 * that created it.
 * <p>
 * Not covered by unit tests, since it needs a graphics card: see the integration tests.
 */
public final class GlPaths implements PathsView, Drawable, AutoCloseable {

    // Interleaved vertex attributes: clip position (4 floats), colour (3)
    private static final int POSITION = 0;
    private static final int COLOUR = 1;
    private static final int POSITION_SIZE = 4;
    private static final int COLOUR_SIZE = 3;
    private static final int FLOATS_PER_POINT = POSITION_SIZE + COLOUR_SIZE;
    private static final int STRIDE = FLOATS_PER_POINT * Float.BYTES;

    private final int program;
    private final int vertexArray;
    private final int buffer;
    // The paths of the last update, drawn again in every frame until the next one
    private List<Path> paths = List.of();

    private GlPaths(int program, int vertexArray, int buffer) {
        this.program = program;
        this.vertexArray = vertexArray;
        this.buffer = buffer;
    }

    /**
     * @throws IllegalStateException if the shaders cannot be built
     */
    public static @NotNull GlPaths create() {
        int program = ShaderProgram.link("/shaders/path.vert", "/shaders/path.frag");
        int vertexArray = glGenVertexArrays();
        int buffer = glGenBuffers();
        glBindVertexArray(vertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        attribute(POSITION, POSITION_SIZE, 0);
        attribute(COLOUR, COLOUR_SIZE, POSITION_SIZE);
        glBindVertexArray(0);
        return new GlPaths(program, vertexArray, buffer);
    }

    @Override
    public void show(@NotNull List<Path> paths) {
        this.paths = List.copyOf(paths);
    }

    @Override
    public void draw() {
        // Without the depth test nothing is written in the depth buffer: the paths lie under the
        // discs drawn after them, which hide one another only by their own depth
        glDisable(GL_DEPTH_TEST);
        glUseProgram(program);
        glBindVertexArray(vertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glBufferData(GL_ARRAY_BUFFER, vertices(), GL_STREAM_DRAW);
        // One strip per path, so that the end of a path is not joined to the start of the next one
        int first = 0;
        for (Path path : paths) {
            int count = path.points().size();
            glDrawArrays(GL_LINE_STRIP, first, count);
            first += count;
        }
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

    // Every point of every path, one path after the other
    private float @NotNull [] vertices() {
        int points = paths.stream().mapToInt(path -> path.points().size()).sum();
        float[] vertices = new float[points * FLOATS_PER_POINT];
        int index = 0;
        for (Path path : paths) {
            Colour colour = path.colour();
            for (ClipPoint point : path.points()) {
                float[] values = {(float) point.x(), (float) point.y(), (float) point.z(), (float) point.w(),
                        (float) colour.red(), (float) colour.green(), (float) colour.blue()};
                System.arraycopy(values, 0, vertices, index, FLOATS_PER_POINT);
                index += FLOATS_PER_POINT;
            }
        }
        return vertices;
    }
}
