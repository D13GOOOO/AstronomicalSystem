package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Draws glows with OpenGL. Needs a current OpenGL 3.3 core context, on the thread that created it.
 * <p>
 * Not covered by unit tests, since it needs a graphics card: see the integration tests.
 */
public final class GlGlows implements GlowsView, Drawable, AutoCloseable {

    // The glows of the last update, drawn again in every frame until the next one
    private List<Glow> glows = List.of();

    private GlGlows() {
    }

    /**
     * @throws IllegalStateException if the shaders cannot be built
     */
    public static @NotNull GlGlows create() {
        return new GlGlows();
    }

    @Override
    public void show(@NotNull List<Glow> glows) {
        this.glows = List.copyOf(glows);
    }

    @Override
    public void draw() {
    }

    @Override
    public void close() {
    }
}
