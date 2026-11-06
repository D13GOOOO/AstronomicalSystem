package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Window with an OpenGL 3.3 core context, created through GLFW. Only one window at a time: GLFW
 * is initialised when it opens and terminated when it closes.
 * <p>
 * Not covered by unit tests, since it needs a display and a graphics card: see the integration
 * tests.
 */
public final class GlfwWindow implements Window, AutoCloseable {

    private static final int OPENGL_MAJOR_VERSION = 3;
    private static final int OPENGL_MINOR_VERSION = 3;
    // One buffer swap per screen refresh
    private static final int VERTICAL_SYNC = 1;
    // Deep space, a little lighter than black so that black bodies would still show
    private static final float BACKGROUND_RED = 0.01f;
    private static final float BACKGROUND_GREEN = 0.01f;
    private static final float BACKGROUND_BLUE = 0.03f;
    private static final float OPAQUE = 1.0f;

    private final long handle;

    private GlfwWindow(long handle) {
        this.handle = handle;
    }

    /**
     * Opens the window and makes its OpenGL context current on the calling thread.
     *
     * @throws IllegalStateException if GLFW or the window cannot be created
     */
    public static @NotNull GlfwWindow open(@NotNull String title, int width, int height) {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            terminateGlfw();
            throw new IllegalStateException("GLFW cannot be initialised");
        }
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, OPENGL_MAJOR_VERSION);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, OPENGL_MINOR_VERSION);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        // Required by macOS for a core profile
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        long handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) {
            terminateGlfw();
            throw new IllegalStateException("The window cannot be created");
        }
        glfwMakeContextCurrent(handle);
        glfwSwapInterval(VERTICAL_SYNC);
        GL.createCapabilities();
        glClearColor(BACKGROUND_RED, BACKGROUND_GREEN, BACKGROUND_BLUE, OPAQUE);
        return new GlfwWindow(handle);
    }

    @Override
    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    @Override
    public void drawFrame() {
        glClear(GL_COLOR_BUFFER_BIT);
        glfwSwapBuffers(handle);
        glfwPollEvents();
    }

    @Override
    public void close() {
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
        terminateGlfw();
    }

    private static void terminateGlfw() {
        glfwTerminate();
        GLFWErrorCallback previous = glfwSetErrorCallback(null);
        if (previous != null) {
            previous.free();
        }
    }
}
