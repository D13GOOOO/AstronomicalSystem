package astronomicalsystem.app.texture;

import astronomicalsystem.app.view.TexturesView;
import org.jetbrains.annotations.NotNull;

/**
 * Gives the view the maps that come with the application, JPEG files among its resources.
 */
public class BundledMaps {

    private final TexturesView view;

    public BundledMaps(@NotNull TexturesView view) {
        this.view = view;
    }

    /**
     * @param name of the map, which is also the name of its file without the extension
     * @throws IllegalArgumentException if no map with that name comes with the application
     */
    public void load(@NotNull String name) {
    }
}
