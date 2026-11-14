package astronomicalsystem.app.texture;

import astronomicalsystem.app.view.TexturesView;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

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
        try (InputStream file = BundledMaps.class.getResourceAsStream("/textures/" + name + ".jpg")) {
            view.load(name, file.readAllBytes());
        } catch (IOException unreadable) {
            // Not covered by the tests: a resource of the application that is there can be read
            throw new UncheckedIOException(unreadable);
        }
    }
}
