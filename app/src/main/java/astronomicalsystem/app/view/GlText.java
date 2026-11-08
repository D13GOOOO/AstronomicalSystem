package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_R8;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap;
import static org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad;

/**
 * Writes lines of text with OpenGL over the picture, from its top left corner, in the font Share
 * Tech Mono (SIL Open Font License, in the resources next to the font). Needs a current OpenGL 3.3
 * core context, on the thread that created it.
 * <p>
 * The glyphs of the printable ASCII characters are drawn once into a texture, the atlas; every
 * character is then a quad that shows its glyph.
 * <p>
 * Not covered by unit tests, since it needs a graphics card: see the integration tests.
 */
public final class GlText implements TextView, Drawable, AutoCloseable {

    private static final String FONT = "/fonts/ShareTechMono-Regular.ttf";
    // From the top of the tallest glyph to the bottom of the lowest one
    private static final float FONT_HEIGHT_IN_PIXELS = 20.0f;
    private static final float LINE_SPACING_IN_PIXELS = 24.0f;
    private static final float MARGIN_IN_PIXELS = 10.0f;
    // Printable ASCII, from the space to the tilde; other characters are written as UNKNOWN
    private static final int FIRST_CHARACTER = ' ';
    private static final int CHARACTER_COUNT = '~' - ' ' + 1;
    private static final char UNKNOWN = '?';
    // Large enough for every glyph at this height: baking checks it
    private static final int ATLAS_SIZE = 256;

    // Interleaved vertex attributes: position in pixels (2 floats), position in the atlas (2)
    private static final int POSITION = 0;
    private static final int ATLAS_POSITION = 1;
    private static final int PAIR = 2;
    private static final int FLOATS_PER_VERTEX = 2 * PAIR;
    private static final int STRIDE = FLOATS_PER_VERTEX * Float.BYTES;
    // Two triangles per character
    private static final int VERTICES_PER_CHARACTER = 6;

    private final int program;
    private final int imageSize;
    private final int vertexArray;
    private final int buffer;
    private final int atlas;
    // Where each glyph is in the atlas and how far it moves the pen
    private final STBTTBakedChar.Buffer glyphs;
    // The lines of the last update, written again in every frame until the next one
    private List<String> lines = List.of();

    private GlText(int program, int vertexArray, int buffer, int atlas, @NotNull STBTTBakedChar.Buffer glyphs) {
        this.program = program;
        this.imageSize = glGetUniformLocation(program, "imageSize");
        this.vertexArray = vertexArray;
        this.buffer = buffer;
        this.atlas = atlas;
        this.glyphs = glyphs;
    }

    /**
     * @throws IllegalStateException if the font or the shaders cannot be loaded
     */
    public static @NotNull GlText create() {
        STBTTBakedChar.Buffer glyphs = STBTTBakedChar.malloc(CHARACTER_COUNT);
        int atlas = bakeAtlas(glyphs);
        int program = ShaderProgram.link("/shaders/text.vert", "/shaders/text.frag");
        int vertexArray = glGenVertexArrays();
        int buffer = glGenBuffers();
        glBindVertexArray(vertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        attribute(POSITION, 0);
        attribute(ATLAS_POSITION, PAIR);
        glBindVertexArray(0);
        return new GlText(program, vertexArray, buffer, atlas, glyphs);
    }

    @Override
    public void show(@NotNull List<String> lines) {
        this.lines = List.copyOf(lines);
    }

    @Override
    public void draw() {
        float[] vertices = vertices();
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        // Over everything, with the edges of the glyphs blended into the picture
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glUseProgram(program);
        glUniform2f(imageSize, viewport[2], viewport[3]);
        glBindTexture(GL_TEXTURE_2D, atlas);
        glBindVertexArray(vertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STREAM_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, vertices.length / FLOATS_PER_VERTEX);
        glBindVertexArray(0);
    }

    @Override
    public void close() {
        glDeleteBuffers(buffer);
        glDeleteVertexArrays(vertexArray);
        glDeleteProgram(program);
        glDeleteTextures(atlas);
        glyphs.free();
    }

    private static int bakeAtlas(@NotNull STBTTBakedChar.Buffer glyphs) {
        ByteBuffer font = fontFile();
        ByteBuffer coverage = MemoryUtil.memAlloc(ATLAS_SIZE * ATLAS_SIZE);
        try {
            if (stbtt_BakeFontBitmap(font, FONT_HEIGHT_IN_PIXELS, coverage, ATLAS_SIZE, ATLAS_SIZE, FIRST_CHARACTER,
                    glyphs) <= 0) {
                throw new IllegalStateException("The glyphs of " + FONT + " do not fit the atlas");
            }
            int texture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texture);
            // One byte per pixel: rows are not aligned to four bytes
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, ATLAS_SIZE, ATLAS_SIZE, 0, GL_RED, GL_UNSIGNED_BYTE, coverage);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            return texture;
        } finally {
            MemoryUtil.memFree(coverage);
            MemoryUtil.memFree(font);
        }
    }

    // stb_truetype reads the font from native memory
    private static @NotNull ByteBuffer fontFile() {
        try (InputStream stream = GlText.class.getResourceAsStream(FONT)) {
            if (stream == null) {
                throw new IllegalStateException("Font " + FONT + " is missing");
            }
            byte[] bytes = stream.readAllBytes();
            return MemoryUtil.memAlloc(bytes.length).put(bytes).flip();
        } catch (IOException e) {
            throw new IllegalStateException("Font " + FONT + " cannot be read", e);
        }
    }

    private static void attribute(int location, int offsetInFloats) {
        glVertexAttribPointer(location, PAIR, GL_FLOAT, false, STRIDE, (long) offsetInFloats * Float.BYTES);
        glEnableVertexAttribArray(location);
    }

    private float @NotNull [] vertices() {
        int characters = lines.stream().mapToInt(String::length).sum();
        float[] vertices = new float[characters * VERTICES_PER_CHARACTER * FLOATS_PER_VERTEX];
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // The pen: stb_truetype moves it along the baseline, character after character
            FloatBuffer penX = stack.mallocFloat(1);
            FloatBuffer penY = stack.mallocFloat(1);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            int index = 0;
            for (int line = 0; line < lines.size(); line++) {
                penX.put(0, MARGIN_IN_PIXELS);
                penY.put(0, MARGIN_IN_PIXELS + FONT_HEIGHT_IN_PIXELS + line * LINE_SPACING_IN_PIXELS);
                index = writeLine(lines.get(line), penX, penY, quad, vertices, index);
            }
        }
        return vertices;
    }

    private int writeLine(@NotNull String line, @NotNull FloatBuffer penX, @NotNull FloatBuffer penY,
                          @NotNull STBTTAlignedQuad quad, float @NotNull [] vertices, int start) {
        int index = start;
        for (char character : line.toCharArray()) {
            stbtt_GetBakedQuad(glyphs, ATLAS_SIZE, ATLAS_SIZE, glyphOf(character), penX, penY, quad, true);
            float[] corners = {
                    quad.x0(), quad.y0(), quad.s0(), quad.t0(),
                    quad.x1(), quad.y0(), quad.s1(), quad.t0(),
                    quad.x1(), quad.y1(), quad.s1(), quad.t1(),
                    quad.x0(), quad.y0(), quad.s0(), quad.t0(),
                    quad.x1(), quad.y1(), quad.s1(), quad.t1(),
                    quad.x0(), quad.y1(), quad.s0(), quad.t1()};
            System.arraycopy(corners, 0, vertices, index, corners.length);
            index += corners.length;
        }
        return index;
    }

    private static int glyphOf(char character) {
        boolean inTheAtlas = character >= FIRST_CHARACTER && character < FIRST_CHARACTER + CHARACTER_COUNT;
        return (inTheAtlas ? character : UNKNOWN) - FIRST_CHARACTER;
    }
}
