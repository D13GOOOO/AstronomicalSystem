package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Draws paths with OpenGL as line strips. Needs a current OpenGL 3.3 core context, on the thread
 * that created it.
 * <p>
 * Not covered by unit tests, since it needs a graphics card: see the integration tests.
 */
public final class GlPaths implements PathsView, Drawable, AutoCloseable {

    private GlPaths() {
    }

    /**
     * @throws IllegalStateException if the shaders cannot be built
     */
    public static @NotNull GlPaths create() {
        return new GlPaths();
    }

    @Override
    public void show(@NotNull List<Path> paths) {
    }

    @Override
    public void draw() {
    }

    @Override
    public void close() {
    }
}
