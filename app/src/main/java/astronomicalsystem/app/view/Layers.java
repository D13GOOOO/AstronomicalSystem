package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Drawings painted one over the other, from the bottom one.
 */
public final class Layers implements Drawable {

    public Layers(@NotNull List<Drawable> bottomToTop) {
    }

    @Override
    public void draw() {
    }
}
