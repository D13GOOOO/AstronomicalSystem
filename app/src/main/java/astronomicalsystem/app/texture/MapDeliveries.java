package astronomicalsystem.app.texture;

import astronomicalsystem.app.view.Drawable;
import astronomicalsystem.app.view.TexturesView;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Hands to the view, before a frame is drawn, the maps that have arrived since the frame before.
 * Maps arrive from the threads that download them, and the view can be used only from the thread
 * that draws: this is where they change hands.
 */
public class MapDeliveries implements Drawable {

    private final TexturesView view;

    public MapDeliveries(@NotNull TexturesView view) {
        this.view = view;
    }

    /**
     * From any thread.
     *
     * @param delivery gives the view a map, later, on the thread that draws
     */
    public void post(@NotNull Consumer<TexturesView> delivery) {
    }

    @Override
    public void draw() {
    }
}
