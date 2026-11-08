package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Writes lines of text with OpenGL over the picture, from its top left corner.
 */
public final class GlText implements TextView, Drawable, AutoCloseable {

    private GlText() {
    }

    public static @NotNull GlText create() {
        return new GlText();
    }

    @Override
    public void show(@NotNull List<String> lines) {
    }

    @Override
    public void draw() {
    }

    @Override
    public void close() {
    }
}
