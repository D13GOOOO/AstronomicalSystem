package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.lwjgl.opengl.GL11.GL_PACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.stb.STBImageWrite.stbi_flip_vertically_on_write;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png_to_func;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * The last layer of a frame: when a capture is asked for, it writes the image drawn so far to a
 * PNG file before it is shown. stb encodes the picture in memory and Java writes the file, so
 * that any path works, whatever its characters.
 * <p>
 * Not covered by unit tests, since it needs a graphics card: see the integration tests.
 */
public final class GlScreenshot implements ScreenshotView, Drawable {

    // Red, green and blue: the frame is opaque, so the picture needs no alpha
    private static final int CHANNELS = 3;
    // Rows packed with no padding, whatever the width
    private static final int TIGHTLY_PACKED = 1;

    // Files asked for since the last frame, all written with the next one
    private final Deque<Path> captures = new ArrayDeque<>();

    @Override
    public void capture(@NotNull Path file) {
        captures.add(file);
    }

    @Override
    public void draw() {
        if (captures.isEmpty()) {
            return;
        }
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        int width = viewport[2];
        int height = viewport[3];
        ByteBuffer pixels = MemoryUtil.memAlloc(width * height * CHANNELS);
        try {
            glPixelStorei(GL_PACK_ALIGNMENT, TIGHTLY_PACKED);
            glReadPixels(0, 0, width, height, GL_RGB, GL_UNSIGNED_BYTE, pixels);
            byte[] picture = png(width, height, pixels);
            while (!captures.isEmpty()) {
                write(picture, captures.remove());
            }
        } finally {
            MemoryUtil.memFree(pixels);
        }
    }

    // OpenGL gives the rows from the bottom of the image, a PNG stores them from the top
    private static byte @NotNull [] png(int width, int height, @NotNull ByteBuffer pixels) {
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        try (STBIWriteCallback toMemory = STBIWriteCallback.create((context, data, size) -> {
            byte[] chunk = new byte[size];
            STBIWriteCallback.getData(data, size).get(chunk);
            png.write(chunk, 0, size);
        })) {
            stbi_flip_vertically_on_write(true);
            stbi_write_png_to_func(toMemory, NULL, width, height, CHANNELS, pixels, width * CHANNELS);
        }
        return png.toByteArray();
    }

    private static void write(byte @NotNull [] picture, @NotNull Path file) {
        try {
            // The absolute path of a file always has a parent
            Files.createDirectories(file.toAbsolutePath().getParent());
            Files.write(file, picture);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
