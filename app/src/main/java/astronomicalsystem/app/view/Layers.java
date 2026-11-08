package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Drawings painted one over the other, from the bottom one.
 */
public final class Layers implements Drawable {

    private final List<Drawable> bottomToTop;

    public Layers(@NotNull List<Drawable> bottomToTop) {
        this.bottomToTop = List.copyOf(bottomToTop);
    }

    @Override
    public void draw() {
        for (Drawable layer : bottomToTop) {
            layer.draw();
        }
    }
}
