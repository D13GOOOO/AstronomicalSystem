package astronomicalsystem.app.texture;

import astronomicalsystem.app.view.Drawable;
import astronomicalsystem.app.view.TexturesView;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Stands in for the view of the maps on the threads that download them: the view can be used only
 * from the thread that draws, so the maps given here wait, and are handed to the view before the
 * next frame is drawn.
 */
public class MapDeliveries implements TexturesView, Drawable {

    private final TexturesView view;
    // Filled by the threads that download, emptied by the thread that draws
    private final Queue<Consumer<TexturesView>> waiting = new ConcurrentLinkedQueue<>();

    public MapDeliveries(@NotNull TexturesView view) {
        this.view = view;
    }

    @Override
    public void load(@NotNull String name, byte @NotNull [] image) {
        waiting.add(textures -> textures.load(name, image));
    }

    @Override
    public void loadMosaic(@NotNull String name, int columns, @NotNull List<byte[]> tiles) {
        List<byte[]> asGiven = List.copyOf(tiles);
        waiting.add(textures -> textures.loadMosaic(name, columns, asGiven));
    }

    @Override
    public void draw() {
        // The queue gives null when nothing is waiting
        Consumer<TexturesView> delivery = waiting.poll();
        while (delivery != null) {
            delivery.accept(view);
            delivery = waiting.poll();
        }
    }
}
