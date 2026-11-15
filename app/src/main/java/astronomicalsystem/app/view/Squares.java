package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Squares of two triangles drawn around points, where a shader keeps a round shape: the vertex
 * array and the buffer of interleaved vertices that the views of discs, glows and rings share.
 * Needs a current OpenGL 3.3 core context, on the thread that created it.
 * <p>
 * Not covered by unit tests, since it needs a graphics card: see the integration tests of the views.
 */
final class Squares implements AutoCloseable {

    // Two triangles cover the square from (-1, -1) to (1, 1)
    private static final float[][] CORNERS = {{-1.0f, -1.0f}, {1.0f, -1.0f}, {1.0f, 1.0f},
            {-1.0f, -1.0f}, {1.0f, 1.0f}, {-1.0f, 1.0f}};
    static final int VERTICES_PER_SQUARE = CORNERS.length;

    private final int vertexArray;
    private final int buffer;
    private final int floatsPerVertex;

    private Squares(int vertexArray, int buffer, int floatsPerVertex) {
        this.vertexArray = vertexArray;
        this.buffer = buffer;
        this.floatsPerVertex = floatsPerVertex;
    }

    /**
     * @param sizes how many floats each attribute of a vertex takes, in the order in which a vertex
     *              holds them: the shader finds the first at location 0, the next at 1, and so on
     */
    static @NotNull Squares ofAttributes(int @NotNull ... sizes) {
        int floatsPerVertex = IntStream.of(sizes).sum();
        int vertexArray = glGenVertexArrays();
        int buffer = glGenBuffers();
        glBindVertexArray(vertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        int offsetInFloats = 0;
        for (int location = 0; location < sizes.length; location++) {
            glVertexAttribPointer(location, sizes[location], GL_FLOAT, false, floatsPerVertex * Float.BYTES,
                    (long) offsetInFloats * Float.BYTES);
            glEnableVertexAttribArray(location);
            offsetInFloats += sizes[location];
        }
        glBindVertexArray(0);
        return new Squares(vertexArray, buffer, floatsPerVertex);
    }

    /**
     * Puts a square around each shape and binds the vertex array to draw them from: the square of
     * the first shape starts at vertex 0, each of the others VERTICES_PER_SQUARE after the one
     * before. Unbinding is left to the caller, when it has drawn.
     *
     * @param vertexOf the attributes of the vertex of the shape at the corner
     */
    <T> void around(@NotNull List<T> shapes, @NotNull BiFunction<T, float[], float[]> vertexOf) {
        glBindVertexArray(vertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glBufferData(GL_ARRAY_BUFFER, vertices(shapes, vertexOf), GL_STREAM_DRAW);
    }

    @Override
    public void close() {
        glDeleteBuffers(buffer);
        glDeleteVertexArrays(vertexArray);
    }

    // The vertices of every shape, corner after corner, interleaved
    private <T> float @NotNull [] vertices(@NotNull List<T> shapes,
                                           @NotNull BiFunction<T, float[], float[]> vertexOf) {
        float[] vertices = new float[shapes.size() * CORNERS.length * floatsPerVertex];
        int index = 0;
        for (T shape : shapes) {
            for (float[] corner : CORNERS) {
                // A copy: the corners are not for the caller to change
                System.arraycopy(vertexOf.apply(shape, corner.clone()), 0, vertices, index, floatsPerVertex);
                index += floatsPerVertex;
            }
        }
        return vertices;
    }
}
