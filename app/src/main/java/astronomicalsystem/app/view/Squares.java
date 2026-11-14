package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * Vertices of squares of two triangles drawn around points, where a shader keeps a round shape:
 * what the views of discs and glows share.
 */
final class Squares {

    // Two triangles cover the square from (-1, -1) to (1, 1)
    static final float[][] CORNERS = {{-1.0f, -1.0f}, {1.0f, -1.0f}, {1.0f, 1.0f},
            {-1.0f, -1.0f}, {1.0f, 1.0f}, {-1.0f, 1.0f}};

    private Squares() {
    }

    /**
     * @param vertexOf the attributes of the vertex of the shape at the corner, floatsPerVertex of them
     * @return the vertices of every shape, corner after corner, interleaved
     */
    static <T> float @NotNull [] vertices(@NotNull List<T> shapes, int floatsPerVertex,
                                          @NotNull BiFunction<T, float[], float[]> vertexOf) {
        float[] vertices = new float[shapes.size() * CORNERS.length * floatsPerVertex];
        int index = 0;
        for (T shape : shapes) {
            for (float[] corner : CORNERS) {
                System.arraycopy(vertexOf.apply(shape, corner), 0, vertices, index, floatsPerVertex);
                index += floatsPerVertex;
            }
        }
        return vertices;
    }

    /**
     * Points the attribute of the vertex array bound to its floats in the interleaved buffer bound.
     */
    static void attribute(int location, int size, int offsetInFloats, int floatsPerVertex) {
        glVertexAttribPointer(location, size, GL_FLOAT, false, floatsPerVertex * Float.BYTES,
                (long) offsetInFloats * Float.BYTES);
        glEnableVertexAttribArray(location);
    }
}
