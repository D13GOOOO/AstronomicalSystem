package astronomicalsystem.app.view;

import org.junit.jupiter.api.Test;
import org.lwjgl.opengl.GL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.glfw.GLFW.glfwGetWindowAttrib;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearDepth;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL32.GL_CONTEXT_CORE_PROFILE_BIT;
import static org.lwjgl.opengl.GL32.GL_CONTEXT_PROFILE_MASK;

class GlfwWindowIntegrationTest {

    private static final String TITLE = "Integration test";
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final Drawable NOTHING = () -> {
    };
    // Window depths go from 0, on the near plane, to 1, on the far plane
    private static final float NEAREST = 0.0f;
    private static final float FARTHEST = 1.0f;

    @Test
    void theContextIsOpenGl33CoreProfile() {
        try (GlfwWindow SUT = GlfwWindow.open(TITLE, WIDTH, HEIGHT)) {
            assertThat(GL.getCapabilities().OpenGL33).isTrue();
            assertThat(glGetInteger(GL_CONTEXT_PROFILE_MASK) & GL_CONTEXT_CORE_PROFILE_BIT)
                    .isEqualTo(GL_CONTEXT_CORE_PROFILE_BIT);
        }
    }

    @Test
    void aFrameIsDrawnWithoutErrorsAndTheWindowStaysOpen() {
        try (GlfwWindow SUT = GlfwWindow.open(TITLE, WIDTH, HEIGHT)) {
            SUT.drawFrame(NOTHING);

            assertThat(glGetError()).isEqualTo(GL_NO_ERROR);
            assertThat(SUT.shouldClose()).isFalse();
        }
    }

    @Test
    void theContentIsDrawnOnAClearedDepthBuffer() {
        try (GlfwWindow SUT = GlfwWindow.open(TITLE, WIDTH, HEIGHT)) {
            fillTheDepthBufferWith(NEAREST);
            float[] depthSeenByTheContent = new float[1];

            SUT.drawFrame(() -> glReadPixels(0, 0, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, depthSeenByTheContent));

            assertThat(depthSeenByTheContent[0]).isEqualTo(FARTHEST);
        }
    }

    // The camera follows the size of the window
    @Test
    void theWindowCanBeResized() {
        try (GlfwWindow SUT = GlfwWindow.open(TITLE, WIDTH, HEIGHT)) {
            long windowOfTheCurrentContext = glfwGetCurrentContext();

            assertThat(glfwGetWindowAttrib(windowOfTheCurrentContext, GLFW_RESIZABLE)).isEqualTo(GLFW_TRUE);
        }
    }

    private static void fillTheDepthBufferWith(float depth) {
        glClearDepth(depth);
        glClear(GL_DEPTH_BUFFER_BIT);
        glClearDepth(FARTHEST);
    }
}
