package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Writes labels over the picture, each at its anchor, with a font shared by every text of the
 * window.
 * <p>
 * Not covered by unit tests, since its font needs a graphics card: see the integration tests.
 */
public final class GlLabels implements LabelsView, Drawable {

    private final GlFont font;
    // The labels of the last update, written again in every frame until the next one
    private List<Label> labels = List.of();

    public GlLabels(@NotNull GlFont font) {
        this.font = font;
    }

    @Override
    public void show(@NotNull List<Label> labels) {
        this.labels = List.copyOf(labels);
    }

    @Override
    public void draw() {
        font.write((imageWidth, imageHeight) -> labels.stream().map(this::lineOf).toList());
    }

    // The baseline lies half the height of the capitals below the anchor
    private @NotNull TextLine lineOf(@NotNull Label label) {
        ImagePoint anchor = label.anchor();
        return new TextLine(label.text(), (float) anchor.x(), (float) anchor.y() + font.capitalHeight() / 2.0f);
    }
}
