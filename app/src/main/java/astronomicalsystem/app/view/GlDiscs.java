package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Draws discs with OpenGL as round points. Needs a current OpenGL 3.3 core context.
 */
public final class GlDiscs implements SceneView, Drawable, AutoCloseable {

    private GlDiscs() {
    }

    public static @NotNull GlDiscs create() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void show(@NotNull List<Disc> discs) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void draw() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
