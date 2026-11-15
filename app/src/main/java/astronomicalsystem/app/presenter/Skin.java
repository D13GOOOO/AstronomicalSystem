package astronomicalsystem.app.presenter;

import astronomicalsystem.core.rotation.Spin;
import org.jetbrains.annotations.NotNull;

/**
 * What a particular body looks like: the map of its surface and how it turns.
 *
 * @param texture name of the map of the surface, as the view knows it
 */
public record Skin(@NotNull String texture, @NotNull Spin spin) {
}
