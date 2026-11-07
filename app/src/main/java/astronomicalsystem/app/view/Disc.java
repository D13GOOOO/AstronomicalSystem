package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

/**
 * Round spot of colour drawn facing the viewer.
 *
 * @param diameter in pixels
 */
public record Disc(@NotNull ClipPoint centre, double diameter, @NotNull Colour colour) {
}
