package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;

/**
 * Builds OpenGL programs from shaders stored as resources of the application.
 */
final class ShaderProgram {

    private ShaderProgram() {
    }

    /**
     * @return the name of the linked program
     * @throws IllegalStateException if a shader is missing, does not compile or does not link
     */
    static int link(@NotNull String vertexResource, @NotNull String fragmentResource) {
        int vertexShader = compile(GL_VERTEX_SHADER, vertexResource);
        int fragmentShader = compile(GL_FRAGMENT_SHADER, fragmentResource);
        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        // Once linked, the program keeps what it needs of the shaders
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        if (glGetProgrami(program, GL_LINK_STATUS) == 0) {
            throw new IllegalStateException("Shader program does not link: " + glGetProgramInfoLog(program));
        }
        return program;
    }

    private static int compile(int type, @NotNull String resource) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source(resource));
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            throw new IllegalStateException("Shader " + resource + " does not compile: " + glGetShaderInfoLog(shader));
        }
        return shader;
    }

    private static @NotNull String source(@NotNull String resource) {
        try (InputStream stream = ShaderProgram.class.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Shader " + resource + " is missing");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Shader " + resource + " cannot be read", e);
        }
    }
}
