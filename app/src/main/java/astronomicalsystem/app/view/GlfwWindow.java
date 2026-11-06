package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

/**
 * Window with an OpenGL 3.3 core context, created through GLFW.
 */
public final class GlfwWindow implements Window, AutoCloseable {

    private GlfwWindow() {
    }

    public static @NotNull GlfwWindow open(@NotNull String title, int width, int height) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public boolean shouldClose() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void drawFrame() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
