package astronomicalsystem.app.view;

import org.junit.jupiter.api.Test;
import org.lwjgl.opengl.GL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL32.GL_CONTEXT_CORE_PROFILE_BIT;
import static org.lwjgl.opengl.GL32.GL_CONTEXT_PROFILE_MASK;

class GlfwWindowIntegrationTest {

    private static final String TITLE = "Integration test";
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final Drawable NOTHING = () -> {
    };

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
}
