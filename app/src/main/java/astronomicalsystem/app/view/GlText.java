package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Writes lines of text over the picture, near one of its corners, with a font shared by every
 * text of the window.
 * <p>
 * Not covered by unit tests, since its font needs a graphics card: see the integration tests.
 */
public final class GlText implements TextView, Drawable {

    private final GlFont font;
    private final Corner corner;
    // The lines of the last update, written again in every frame until the next one
    private List<String> lines = List.of();

    /**
     * @param corner where the lines are written
     */
    public GlText(@NotNull GlFont font, @NotNull Corner corner) {
        this.font = font;
        this.corner = corner;
    }

    @Override
    public void show(@NotNull List<String> lines) {
        this.lines = List.copyOf(lines);
    }

    @Override
    public void draw() {
        font.write(this::linesIn);
    }

    private @NotNull List<TextLine> linesIn(float imageWidth, float imageHeight) {
        float left = corner.left(textWidth(), imageWidth);
        float top = corner.top(textHeight(), imageHeight);
        List<TextLine> placed = new ArrayList<>();
        for (int line = 0; line < lines.size(); line++) {
            placed.add(new TextLine(lines.get(line), left, top + font.height() + line * font.lineSpacing()));
        }
        return placed;
    }

    // The widest line
    private float textWidth() {
        float widest = 0.0f;
        for (String line : lines) {
            widest = Math.max(widest, font.widthOf(line));
        }
        return widest;
    }

    // From the top of the first line to the bottom of the last one
    private float textHeight() {
        return font.height() + Math.max(0, lines.size() - 1) * font.lineSpacing();
    }
}
