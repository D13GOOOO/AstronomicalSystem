package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTexSubImage2D;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

/**
 * Keeps maps as OpenGL textures, decoded with stb_image. Needs a current OpenGL 3.3 core context,
 * on the thread that created it.
 * <p>
 * Not covered by unit tests, since it needs a graphics card: see the integration tests.
 */
public final class GlTextures implements TexturesView, AutoCloseable {

    private static final int RED_GREEN_BLUE_ALPHA = 4;
    private static final int ONE_COLUMN = 1;

    // The OpenGL texture of each map, by its name
    private final Map<String, Integer> textures = new HashMap<>();

    private GlTextures() {
    }

    public static @NotNull GlTextures create() {
        return new GlTextures();
    }

    @Override
    public void load(@NotNull String name, byte @NotNull [] image) {
        loadMosaic(name, ONE_COLUMN, List.of(image));
    }

    @Override
    public void loadMosaic(@NotNull String name, int columns, @NotNull List<byte[]> tiles) {
        int texture = newTexture();
        try {
            for (int index = 0; index < tiles.size(); index++) {
                place(name, tiles.get(index), index % columns, index / columns, columns, tiles.size() / columns);
            }
        } catch (IllegalArgumentException unreadable) {
            glDeleteTextures(texture);
            throw unreadable;
        }
        // Smaller copies of the map, each half the size of the one before, sampled when the body is far
        glGenerateMipmap(GL_TEXTURE_2D);
        replace(name, texture);
    }

    /**
     * Makes the map the texture that the next drawing samples.
     *
     * @return whether there is a map with the name
     */
    boolean bind(@NotNull String name) {
        Integer texture = textures.get(name);
        if (texture == null) {
            return false;
        }
        glBindTexture(GL_TEXTURE_2D, texture);
        return true;
    }

    @Override
    public void close() {
        textures.values().forEach(texture -> glDeleteTextures(texture));
        textures.clear();
    }

    // The longitudes go round the body: the map repeats sideways, and stops at the poles
    private static int newTexture() {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        return texture;
    }

    // Decodes a tile and copies it to its place in the texture bound, which is given the size of
    // the whole map when the first tile tells how large the tiles are
    private static void place(@NotNull String name, byte @NotNull [] tile, int column, int row, int columns,
                              int rows) {
        ByteBuffer encoded = MemoryUtil.memAlloc(tile.length);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            encoded.put(tile).flip();
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            ByteBuffer pixels = stbi_load_from_memory(encoded, width, height, channels, RED_GREEN_BLUE_ALPHA);
            if (pixels == null) {
                throw new IllegalArgumentException("Map " + name + " cannot be read: " + stbi_failure_reason());
            }
            if (column == 0 && row == 0) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, columns * width.get(0), rows * height.get(0), 0, GL_RGBA,
                        GL_UNSIGNED_BYTE, (ByteBuffer) null);
            }
            glTexSubImage2D(GL_TEXTURE_2D, 0, column * width.get(0), row * height.get(0), width.get(0),
                    height.get(0), GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            stbi_image_free(pixels);
        } finally {
            MemoryUtil.memFree(encoded);
        }
    }

    private void replace(@NotNull String name, int texture) {
        Integer replaced = textures.put(name, texture);
        if (replaced != null) {
            glDeleteTextures(replaced);
        }
    }
}
