package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

/**
 * Sphere drawn facing the viewer, as a round spot of colour.
 *
 * @param diameter in pixels
 */
public record Disc(@NotNull ClipPoint centre, double diameter, @NotNull Colour colour, @NotNull Lighting lighting) {
}
