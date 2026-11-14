package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

/**
 * Keeps maps as OpenGL textures. Needs a current OpenGL 3.3 core context, on the thread that
 * created it.
 * <p>
 * Not covered by unit tests, since it needs a graphics card: see the integration tests.
 */
public final class GlTextures implements TexturesView, AutoCloseable {

    private GlTextures() {
    }

    public static @NotNull GlTextures create() {
        return new GlTextures();
    }

    @Override
    public void load(@NotNull String name, byte @NotNull [] image) {
    }

    /**
     * Makes the map the texture that the next drawing samples.
     *
     * @return whether there is a map with the name
     */
    boolean bind(@NotNull String name) {
        return false;
    }

    @Override
    public void close() {
    }
}
