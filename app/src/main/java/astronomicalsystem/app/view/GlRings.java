package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Draws rings with OpenGL around the spheres of the discs. Needs a current OpenGL 3.3 core context,
 * on the thread that created it.
 * <p>
 * Not covered by unit tests, since it needs a graphics card: see the integration tests.
 */
public final class GlRings implements RingsView, Drawable, AutoCloseable {

    private GlRings() {
    }

    /**
     * @throws IllegalStateException if the shaders cannot be built
     */
    public static @NotNull GlRings create() {
        return new GlRings();
    }

    @Override
    public void show(@NotNull List<RingSystem> rings) {
    }

    @Override
    public void draw() {
    }

    @Override
    public void close() {
    }
}
